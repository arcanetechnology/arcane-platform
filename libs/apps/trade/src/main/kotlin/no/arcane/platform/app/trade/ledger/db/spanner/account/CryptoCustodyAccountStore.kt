package no.arcane.platform.app.trade.ledger.db.spanner.account

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import com.google.cloud.Timestamp
import com.google.cloud.spanner.Key
import com.google.cloud.spanner.KeySet
import com.google.cloud.spanner.Mutation
import com.google.cloud.spanner.ResultSet
import com.google.cloud.spanner.Statement
import com.google.cloud.spanner.TransactionContext
import no.arcane.platform.app.trade.admin.api.rest.AccountOperation
import no.arcane.platform.app.trade.admin.api.rest.Transaction
import no.arcane.platform.app.trade.admin.api.rest.readAccountOperations
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCurrency
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccount
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.Table
import no.arcane.platform.app.trade.ledger.db.spanner.TransactionId
import no.arcane.platform.app.trade.ledger.db.spanner.insertMutation
import no.arcane.platform.app.trade.ledger.db.spanner.queryStatement
import no.arcane.platform.app.trade.ledger.db.spanner.toInstant
import no.arcane.platform.app.trade.ledger.db.spanner.updateMutation
import java.time.Instant

object CryptoCustodyAccountStore {

    private val CryptoCustodyAccountsTable = object : Table<CryptoCustodyAccount> {
        override val name: String = "CryptoCustodyAccounts"
        override val columns: List<String> =
            listOf(
                "CryptoCustodyAccountId",
                "Balance",
                "ReservedBalance",
                "CryptoCurrency",
                "Alias",
                "CreatedOn",
                "UpdatedOn"
            )

        override fun ResultSet.toObject() = CryptoCustodyAccount(
            id = CryptoCustodyAccountId(getString("CryptoCustodyAccountId")),
            balance = getLong("Balance"),
            reservedBalance = getLong("ReservedBalance"),
            currency = CryptoCurrency(getString("CryptoCurrency")),
            alias = getString("Alias"),
            createdOn = getTimestamp("CreatedOn").toInstant(),
            updatedOn = getTimestamp("UpdatedOn").toInstant(),
        )
    }

    suspend fun add(
        cryptoCustodyAccountId: CryptoCustodyAccountId,
        cryptoCurrency: CryptoCurrency,
        alias: String,
    ): Either<String, Unit> {
        return insertMutation(
            Mutation.newInsertBuilder("CryptoCustodyAccounts")
                .set("CryptoCustodyAccountId").to(cryptoCustodyAccountId.value)
                .set("Balance").to(0L)
                .set("ReservedBalance").to(0L)
                .set("CryptoCurrency").to(cryptoCurrency.toText())
                .set("Alias").to(alias)
                .set("CreatedOn").to(Timestamp.now())
                .set("UpdatedOn").to(Timestamp.now())
                .build(),
            error = "Create Crypto Custody Account failed",
        )
    }

    suspend fun update(
        cryptoCustodyAccountId: CryptoCustodyAccountId,
        alias: String,
    ): Either<String, Unit> {
        return updateMutation(
            Mutation.newUpdateBuilder("CryptoCustodyAccounts")
                .set("CryptoCustodyAccountId").to(cryptoCustodyAccountId.value)
                .set("Alias").to(alias)
                .set("UpdatedOn").to(Timestamp.now())
                .build(),
            error = "Update Crypto Custody Account failed",
        )
    }

    suspend fun getAll(): Either<String, List<CryptoCustodyAccount>> = CryptoCustodyAccountsTable.getAll()

    suspend fun get(
        cryptoCustodyAccountId: CryptoCustodyAccountId,
    ): Either<String, CryptoCustodyAccount> = CryptoCustodyAccountsTable.get(
        cryptoCustodyAccountId.value
    )

    suspend fun get(
        transactionContext: TransactionContext,
        cryptoCustodyAccountId: CryptoCustodyAccountId,
    ): Either<String, CryptoCustodyAccount> = CryptoCustodyAccountsTable.get(
        readContext = transactionContext,
        cryptoCustodyAccountId.value,
    )

    suspend fun operate(
        transactionContext: TransactionContext,
        cryptoCustodyAccountId: CryptoCustodyAccountId,
        transactionId: TransactionId,
        amount: Long,
        timestamp: Timestamp,
    ): Either<String, Unit> {
        return either {
            val balance = transactionContext.read(
                "CryptoCustodyAccounts",
                KeySet.singleKey(Key.of(cryptoCustodyAccountId.value)),
                listOf("Balance")
            ).use {
                if (!it.next()) {
                    "$cryptoCustodyAccountId not found".left().bind<Unit>()
                }
                it.getLong("Balance")
            }
            val newBalance = balance + amount
            if (newBalance < 0) {
                "Insufficient balance".left().bind<Unit>()
            }
            transactionContext.buffer(
                listOf(
                    Mutation
                        .newUpdateBuilder("CryptoCustodyAccounts")
                        .set("CryptoCustodyAccountId").to(cryptoCustodyAccountId.value)
                        .set("Balance").to(newBalance)
                        .set("UpdatedOn").to(timestamp)
                        .build(),
                    Mutation
                        .newInsertBuilder("CryptoCustodyAccountOperations")
                        .set("CryptoCustodyAccountId").to(cryptoCustodyAccountId.value)
                        .set("TransactionId").to(transactionId.value)
                        .set("Amount").to(amount)
                        .set("Balance").to(newBalance)
                        .set("CreatedOn").to(timestamp)
                        .build(),
                )
            )
        }
    }

    suspend fun getOperations(
        cryptoCustodyAccountId: CryptoCustodyAccountId,
        offset: Instant,
        limit: ULong
    ): Either<String, List<AccountOperation>> {
        return queryStatement(
            Statement.newBuilder("""
                SELECT *
                FROM CryptoCustodyAccountOperations
                WHERE CryptoCustodyAccountId = @accountId
                AND CreatedOn > @offset
                LIMIT @limit
                """.trimIndent()
            )
                .bind("accountId").to(cryptoCustodyAccountId.value)
                .bind("offset").to(Timestamp.ofTimeSecondsAndNanos(offset.epochSecond, offset.nano))
                .bind("limit").to(limit.toLong())
                .build()
        ) { resultSet ->
            readAccountOperations(resultSet).right()
        }
    }
}