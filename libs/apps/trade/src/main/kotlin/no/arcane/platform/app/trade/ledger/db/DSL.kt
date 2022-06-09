package no.arcane.platform.app.trade.ledger.db

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.arcane.platform.utils.logging.getLogger
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

inline fun <LEFT, RIGHT> DataSource.usingConnection(
    block: Connection.() -> Either<LEFT, RIGHT>
): Either<LEFT, RIGHT> {
    return this.connection.use { connection ->
        try {
            connection.autoCommit = false
            connection.transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
            connection.block().bitap(
                {
                    val logger by getLogger()
                    logger.error(it.toString())
                    connection.rollback()
                },
                {
                    connection.commit()
                }
            )
        } finally {
            connection.autoCommit = true
        }
    }
}

suspend inline fun Connection.selectStatement(
    @Language("SQL")
    sql: String,
    block: PreparedStatement.() -> Unit
): Either<String, ResultSet> {
    val preparedStatement = this.prepareStatement(sql)
    return try {
        preparedStatement.block()
        withContext(Dispatchers.IO) {
            preparedStatement.executeQuery()
        }.right()
    } catch (e: Exception) {
        e.printStackTrace()
        e.message!!.left()
    }
}

suspend inline fun Connection.validate(
    @Language("SQL")
    sql: String,
    error: String,
    crossinline block: PreparedStatement.() -> Unit
): Either<String, Unit> {
    return either {
        val resultSet = selectStatement(sql, block).bind()
        if (resultSet.next()) {
            Unit.right()
        } else {
            error.left()
        }
    }
}

fun <LEFT, RIGHT> Either<LEFT, RIGHT>.bitap(
    left: (LEFT) -> Unit,
    right: (RIGHT) -> Unit
): Either<LEFT, RIGHT> = this.tap(right).tapLeft(left)

suspend inline fun Connection.updateStatement(
    @Language("SQL")
    sql: String,
    block: PreparedStatement.() -> Unit
): Either<String, Unit> {
    val preparedStatement = this.prepareStatement(sql)
    return try {
        preparedStatement.block()
        val changedRowCount = withContext(Dispatchers.IO) {
            preparedStatement.executeUpdate()
        }
        if (changedRowCount == 0) {
            "No rows changed for $preparedStatement".left()
        } else {
            Unit.right()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        e.message!!.left()
    }
}
