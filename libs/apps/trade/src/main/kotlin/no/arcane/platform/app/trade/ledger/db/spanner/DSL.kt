package no.arcane.platform.app.trade.ledger.db.spanner

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.cloud.spanner.AbortedException
import com.google.cloud.spanner.DatabaseClient
import com.google.cloud.spanner.DatabaseId
import com.google.cloud.spanner.Mutation
import com.google.cloud.spanner.ReadOnlyTransaction
import com.google.cloud.spanner.ResultSet
import com.google.cloud.spanner.Spanner
import com.google.cloud.spanner.SpannerOptions
import com.google.cloud.spanner.Statement
import com.google.cloud.spanner.TransactionContext
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

private val logger by lazy { LoggerFactory.getLogger("no.arcane.platform.app.trade.ledger.db.spanner.DSLKt") }

suspend fun insertMutation(
    mutation: Mutation,
    error: String,
): Either<String, Unit> {
    return usingSpanner { spannerClient ->
        try {
            spannerClient.write(listOf(mutation))
            Unit.right()
        } catch (e: Exception) {
            logger.error(e.message, e)
            error.left()
        }
    }
}

suspend fun updateMutation(
    mutation: Mutation,
    error: String,
): Either<String, Unit> {
    return usingSpanner { spannerClient ->
        try {
            spannerClient.writeAtLeastOnce(listOf(mutation))
            Unit.right()
        } catch (e: Exception) {
            logger.error(e.message, e)
            error.left()
        }
    }
}

suspend fun <T> queryStatement(
    statement: Statement,
    block: suspend (ResultSet) -> T
): T {
    return usingSpanner { databaseClient ->
        databaseClient
            .singleUse()
            .executeQuery(statement)
            .use { resultSet ->
                block(resultSet)
            }
    }
}

suspend fun <LEFT, T> readWriteTransaction(
    block: suspend (TransactionContext) -> Either<LEFT, T>
): Either<LEFT, T> {
    return usingSpanner { databaseClient ->
        databaseClient
            .transactionManager()
            .use { transactionManager ->
                var transactionContext = transactionManager.begin()
                var result: Either<LEFT, T>
                while (true) {
                    result = block(transactionContext)
                    if (result.isLeft()) {
                        transactionManager.rollback()
                        break
                    }
                    try {
                        transactionManager.commit()
                        break
                    } catch (e: AbortedException) {
                        delay(e.retryDelayInMillis)
                        transactionContext = transactionManager.resetForRetry()
                    }
                }
                result
            }
    }
}

suspend fun <T> readOnlyTransaction(
    block: suspend (ReadOnlyTransaction) -> T
): T {
    return usingSpanner { databaseClient ->
        databaseClient
            .readOnlyTransaction()
            .use { readOnlyTransaction ->
                block(readOnlyTransaction)
            }
    }
}

suspend fun <T> usingSpanner(block: suspend (DatabaseClient) -> T): T {
    val spannerEmulatorHost: String? = System.getenv("SPANNER_EMULATOR_HOST")
    val spannerOptions = SpannerOptions
        .newBuilder()
        .build()
    val databaseAndInstanceName = if (spannerEmulatorHost != null) {
        logger.warn("Connecting to Spanner emulator: $spannerEmulatorHost")
        "test"
    } else {
        "trade"
    }
    return spannerOptions.service.use { spanner: Spanner ->
        val databaseId = DatabaseId.of(spannerOptions.projectId, databaseAndInstanceName, databaseAndInstanceName)
        val databaseClient = spanner.getDatabaseClient(databaseId)
        block(databaseClient)
    }
}


