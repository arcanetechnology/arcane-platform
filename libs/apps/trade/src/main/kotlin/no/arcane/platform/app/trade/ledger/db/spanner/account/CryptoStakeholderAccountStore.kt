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
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCurrency
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoStakeholderAccount
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.ProfileId
import no.arcane.platform.app.trade.ledger.db.spanner.Table
import no.arcane.platform.app.trade.ledger.db.spanner.TransactionId
import no.arcane.platform.app.trade.ledger.db.spanner.insertMutation
import no.arcane.platform.app.trade.ledger.db.spanner.queryStatement
import no.arcane.platform.app.trade.ledger.db.spanner.toInstant
import no.arcane.platform.app.trade.ledger.db.spanner.updateMutation
import java.time.Instant
import java.util.*

object CryptoStakeholderAccountStore {

    private val CryptoStakeholderAccountsTable = object : Table<CryptoStakeholderAccount> {
        override val name: String = "CryptoStakeholderAccounts"
        override val columns: List<String> =
            listOf(
                "UserId",
                "ProfileId",
                "CryptoStakeholderAccountId",
                "CryptoCustodyAccountId",
                "Balance",
                "ReservedBalance",
                "CryptoCurrency",
                "Alias",
                "CreatedOn",
                "UpdatedOn"
            )

        override fun ResultSet.toObject() = CryptoStakeholderAccount(
            id = CryptoStakeholderAccountId(
                userId = getString("UserId"),
                profileId = getString("ProfileId"),
                value = getString("CryptoStakeholderAccountId"),
            ),
            custodyAccountId = CryptoCustodyAccountId(getString("CryptoCustodyAccountId")),
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
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
        cryptoCurrency: CryptoCurrency,
        alias: String,
    ): Either<String, Unit> {
        return insertMutation(
            Mutation.newInsertBuilder("CryptoStakeholderAccounts")
                .set("UserId").to(cryptoStakeholderAccountId.userId)
                .set("ProfileId").to(cryptoStakeholderAccountId.profileId)
                .set("CryptoStakeholderAccountId").to(cryptoStakeholderAccountId.value)
                .set("Balance").to(0L)
                .set("ReservedBalance").to(0L)
                .set("CryptoCurrency").to(cryptoCurrency.toText())
                .set("Alias").to(alias)
                .set("CryptoCustodyAccountId").to(cryptoCustodyAccountId.value)
                .set("CreatedOn").to(Timestamp.now())
                .set("UpdatedOn").to(Timestamp.now())
                .build(),
            error = "Create CryptoStakeholderAccount failed"
        )
    }

    suspend fun update(
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
        alias: String,
    ): Either<String, Unit> {
        return updateMutation(
            Mutation.newUpdateBuilder("CryptoStakeholderAccounts")
                .set("UserId").to(cryptoStakeholderAccountId.userId)
                .set("ProfileId").to(cryptoStakeholderAccountId.profileId)
                .set("CryptoStakeholderAccountId").to(cryptoStakeholderAccountId.value)
                .set("Alias").to(alias)
                .set("UpdatedOn").to(Timestamp.now())
                .build(),
            error = "Update CryptoStakeholderAccount failed"
        )
    }

    suspend fun get(
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
    ): Either<String, CryptoStakeholderAccount> = CryptoStakeholderAccountsTable.get(
        cryptoStakeholderAccountId.userId,
        cryptoStakeholderAccountId.profileId,
        cryptoStakeholderAccountId.value
    )

    suspend fun get(
        transactionContext: TransactionContext,
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
    ): Either<String, CryptoStakeholderAccount> = CryptoStakeholderAccountsTable.get(
        readContext = transactionContext,
        cryptoStakeholderAccountId.userId,
        cryptoStakeholderAccountId.profileId,
        cryptoStakeholderAccountId.value
    )

    suspend fun getAll(
        profileId: ProfileId,
    ): Either<String, List<CryptoStakeholderAccount>> = CryptoStakeholderAccountsTable.getAll(
        profileId.userId,
        profileId.value,
    )

    suspend fun operate(
        transactionContext: TransactionContext,
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
        transactionId: TransactionId,
        amount: Long,
        timestamp: Timestamp,
    ): Either<String, Unit> {
        return either {
            val balance = transactionContext.read(
                "CryptoStakeholderAccounts",
                KeySet.singleKey(
                    Key.of(
                        cryptoStakeholderAccountId.userId,
                        cryptoStakeholderAccountId.profileId,
                        cryptoStakeholderAccountId.value
                    )
                ),
                listOf("Balance")
            ).use {
                if (!it.next()) {
                    "$cryptoStakeholderAccountId not found".left().bind<Unit>()
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
                        .newUpdateBuilder("CryptoStakeholderAccounts")
                        .set("UserId").to(cryptoStakeholderAccountId.userId)
                        .set("ProfileId").to(cryptoStakeholderAccountId.profileId)
                        .set("CryptoStakeholderAccountId").to(cryptoStakeholderAccountId.value)
                        .set("Balance").to(newBalance)
                        .set("UpdatedOn").to(timestamp)
                        .build(),
                    Mutation
                        .newInsertBuilder("CryptoStakeholderAccountOperations")
                        .set("UserId").to(cryptoStakeholderAccountId.userId)
                        .set("ProfileId").to(cryptoStakeholderAccountId.profileId)
                        .set("CryptoStakeholderAccountId").to(cryptoStakeholderAccountId.value)
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
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
        offset: Instant,
        limit: ULong
    ): Either<String, List<AccountOperation>> {
        return queryStatement(
            Statement.newBuilder("""
                SELECT *
                FROM CryptoStakeholderAccountOperations
                WHERE UserId = @userId
                AND ProfileId = @profileId
                AND CryptoStakeholderAccountId = @accountId
                AND CreatedOn > @offset
                LIMIT @limit
                """.trimIndent()
            )
                .bind("userId").to(cryptoStakeholderAccountId.userId)
                .bind("profileId").to(cryptoStakeholderAccountId.profileId)
                .bind("accountId").to(cryptoStakeholderAccountId.value)
                .bind("offset").to(Timestamp.ofTimeSecondsAndNanos(offset.epochSecond, offset.nano))
                .bind("limit").to(limit.toLong())
                .build()
        ) { resultSet ->
            readAccountOperations(resultSet).right()
        }
    }
}