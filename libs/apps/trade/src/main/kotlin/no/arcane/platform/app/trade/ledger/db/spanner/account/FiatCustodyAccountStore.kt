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
import no.arcane.platform.app.trade.admin.api.rest.readAccountOperations
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCurrency
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCustodyAccount
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.Table
import no.arcane.platform.app.trade.ledger.db.spanner.TransactionId
import no.arcane.platform.app.trade.ledger.db.spanner.insertMutation
import no.arcane.platform.app.trade.ledger.db.spanner.queryStatement
import no.arcane.platform.app.trade.ledger.db.spanner.toInstant
import no.arcane.platform.app.trade.ledger.db.spanner.updateMutation
import java.time.Instant
import java.util.*

object FiatCustodyAccountStore {

    private val FiatCustodyAccountsTable = object : Table<FiatCustodyAccount> {
        override val name: String = "FiatCustodyAccounts"
        override val columns: List<String> =
            listOf(
                "FiatCustodyAccountId",
                "Balance",
                "ReservedBalance",
                "Currency",
                "Alias",
                "CreatedOn",
                "UpdatedOn"
            )

        override fun ResultSet.toObject() = FiatCustodyAccount(
            id = FiatCustodyAccountId(getString("FiatCustodyAccountId")),
            balance = getLong("Balance"),
            reservedBalance = getLong("ReservedBalance"),
            currency = FiatCurrency.valueOf(getString("Currency")),
            alias = getString("Alias"),
            createdOn = getTimestamp("CreatedOn").toInstant(),
            updatedOn = getTimestamp("UpdatedOn").toInstant(),
        )
    }

    suspend fun add(
        fiatCustodyAccountId: FiatCustodyAccountId,
        currency: FiatCurrency,
        alias: String,
    ): Either<String, Unit> {
        return insertMutation(
            Mutation.newInsertBuilder("FiatCustodyAccounts")
                .set("FiatCustodyAccountId").to(fiatCustodyAccountId.value)
                .set("Balance").to(0L)
                .set("ReservedBalance").to(0L)
                .set("Currency").to(currency.toText())
                .set("Alias").to(alias)
                .set("CreatedOn").to(Timestamp.now())
                .set("UpdatedOn").to(Timestamp.now())
                .build(),
            error = "Create Fiat Custody Account failed"
        )
    }

    suspend fun update(
        fiatCustodyAccountId: FiatCustodyAccountId,
        alias: String,
    ): Either<String, Unit> {
        return updateMutation(
            Mutation.newUpdateBuilder("FiatCustodyAccounts")
                .set("FiatCustodyAccountId").to(fiatCustodyAccountId.value)
                .set("Alias").to(alias)
                .set("UpdatedOn").to(Timestamp.now())
                .build(),
            error = "Create Fiat Custody Account failed"
        )
    }

    suspend fun get(
        fiatCustodyAccountId: FiatCustodyAccountId,
    ): Either<String, FiatCustodyAccount> = FiatCustodyAccountsTable.get(
        fiatCustodyAccountId.value
    )

    suspend fun get(
        transactionContext: TransactionContext,
        fiatCustodyAccountId: FiatCustodyAccountId,
    ): Either<String, FiatCustodyAccount> = FiatCustodyAccountsTable.get(
        readContext = transactionContext,
        fiatCustodyAccountId.value,
    )

    suspend fun getAll(): Either<String, List<FiatCustodyAccount>> = FiatCustodyAccountsTable.getAll()

    suspend fun operate(
        transactionContext: TransactionContext,
        fiatCustodyAccountId: FiatCustodyAccountId,
        transactionId: TransactionId,
        amount: Long,
        timestamp: Timestamp,
    ): Either<String, Unit> {
        return either {
            val balance = transactionContext.read(
                "FiatCustodyAccounts",
                KeySet.singleKey(Key.of(fiatCustodyAccountId.value)),
                listOf("Balance")
            ).use {
                if (!it.next()) {
                    "$fiatCustodyAccountId not found".left().bind<Unit>()
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
                        .newUpdateBuilder("FiatCustodyAccounts")
                        .set("FiatCustodyAccountId").to(fiatCustodyAccountId.value)
                        .set("Balance").to(newBalance)
                        .set("UpdatedOn").to(timestamp)
                        .build(),
                    Mutation
                        .newInsertBuilder("FiatCustodyAccountOperations")
                        .set("FiatCustodyAccountId").to(fiatCustodyAccountId.value)
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
        fiatCustodyAccountId: FiatCustodyAccountId,
        offset: Instant,
        limit: ULong
    ): Either<String, List<AccountOperation>> {
        return queryStatement(
            Statement.newBuilder("""
                SELECT *
                FROM FiatCustodyAccountOperations
                WHERE FiatCustodyAccountId = @accountId
                AND CreatedOn > @offset
                LIMIT @limit
                """.trimIndent()
            )
                .bind("accountId").to(fiatCustodyAccountId.value)
                .bind("offset").to(Timestamp.ofTimeSecondsAndNanos(offset.epochSecond, offset.nano))
                .bind("limit").to(limit.toLong())
                .build()
        ) { resultSet ->
            readAccountOperations(resultSet).right()
        }
    }
}