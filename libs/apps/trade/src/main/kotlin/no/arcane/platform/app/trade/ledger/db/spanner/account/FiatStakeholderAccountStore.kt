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
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCurrency
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccount
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.ProfileId
import no.arcane.platform.app.trade.ledger.db.spanner.Table
import no.arcane.platform.app.trade.ledger.db.spanner.TransactionId
import no.arcane.platform.app.trade.ledger.db.spanner.queryStatement
import no.arcane.platform.app.trade.ledger.db.spanner.readWriteTransaction
import no.arcane.platform.app.trade.ledger.db.spanner.toInstant
import no.arcane.platform.app.trade.ledger.db.spanner.updateMutation
import java.time.Instant
import java.util.*

object FiatStakeholderAccountStore {

    private val FiatStakeholderAccountsTable = object : Table<FiatStakeholderAccount> {
        override val name: String = "FiatStakeholderAccounts"
        override val columns: List<String> =
            listOf(
                "UserId",
                "ProfileId",
                "FiatStakeholderAccountId",
                "FiatCustodyAccountId",
                "Balance",
                "ReservedBalance",
                "Currency",
                "Alias",
                "CreatedOn",
                "UpdatedOn"
            )

        override fun ResultSet.toObject() = FiatStakeholderAccount(
            id = FiatStakeholderAccountId(
                userId = getString("UserId"),
                profileId = getString("ProfileId"),
                value = getString("FiatStakeholderAccountId"),
            ),
            custodyAccountId = FiatCustodyAccountId(getString("FiatCustodyAccountId")),
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
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        currency: FiatCurrency,
        alias: String,
    ): Either<String, Unit> {
        return readWriteTransaction { txn ->
            either {
                txn
                    .executeQuery(
                        Statement.newBuilder(
                            """
                            SELECT *
                            FROM FiatStakeholderAccounts
                            WHERE UserId = @userId
                            AND ProfileId = @profileId
                            AND Currency = @currency
                            """.trimIndent()
                        )
                            .bind("userId").to(fiatStakeholderAccountId.userId)
                            .bind("profileId").to(fiatStakeholderAccountId.profileId)
                            .bind("currency").to(currency.toText())
                            .build()
                    )
                    .use { resultSet ->
                        if (resultSet.next()) {
                            "Account not added. Account with this currency already exists".left().bind<Unit>()
                        }
                    }
                val rowsCreated = txn
                    .executeUpdate(
                        Statement.newBuilder(
                            """
                            INSERT FiatStakeholderAccounts (UserId, ProfileId, FiatStakeholderAccountId, Balance, ReservedBalance, Currency, Alias, FiatCustodyAccountId, CreatedOn, UpdatedOn)
                            VALUES (@userId, @profileId, @accountId, @balance, @reservedBalance, @currency, @alias, @custodyAccountId, @now, @now)
                            """.trimIndent()
                        )
                            .bind("userId").to(fiatStakeholderAccountId.userId)
                            .bind("profileId").to(fiatStakeholderAccountId.profileId)
                            .bind("accountId").to(fiatStakeholderAccountId.value)
                            .bind("balance").to(0L)
                            .bind("reservedBalance").to(0L)
                            .bind("currency").to(currency.toText())
                            .bind("alias").to(alias)
                            .bind("custodyAccountId").to(fiatCustodyAccountId.value)
                            .bind("now").to(Timestamp.now())
                            .build()
                    )
                if (rowsCreated != 1L) {
                    "Account not created".left().bind<Unit>()
                }
            }
        }
    }

    suspend fun update(
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        alias: String,
    ): Either<String, Unit> {
        return updateMutation(
            Mutation.newUpdateBuilder("FiatStakeholderAccounts")
                .set("UserId").to(fiatStakeholderAccountId.userId)
                .set("ProfileId").to(fiatStakeholderAccountId.profileId)
                .set("FiatStakeholderAccountId").to(fiatStakeholderAccountId.value)
                .set("Alias").to(alias)
                .set("UpdatedOn").to(Timestamp.now())
                .build(),
            error = "Update Fiat Stakeholder Account failed"
        )
    }

    suspend fun get(
        fiatStakeholderAccountId: FiatStakeholderAccountId,
    ): Either<String, FiatStakeholderAccount> = FiatStakeholderAccountsTable.get(
        fiatStakeholderAccountId.userId,
        fiatStakeholderAccountId.profileId,
        fiatStakeholderAccountId.value,
    )

    suspend fun get(
        transactionContext: TransactionContext,
        fiatStakeholderAccountId: FiatStakeholderAccountId,
    ): Either<String, FiatStakeholderAccount> = FiatStakeholderAccountsTable.get(
        readContext = transactionContext,
        fiatStakeholderAccountId.userId,
        fiatStakeholderAccountId.profileId,
        fiatStakeholderAccountId.value,
    )

    suspend fun getAll(
        profileId: ProfileId,
    ): Either<String, List<FiatStakeholderAccount>> = FiatStakeholderAccountsTable.getAll(
        profileId.userId,
        profileId.value
    )

    suspend fun operate(
        transactionContext: TransactionContext,
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        transactionId: TransactionId,
        amount: Long,
        timestamp: Timestamp,
    ): Either<String, Unit> {
        return either {
            val balance: Long = transactionContext.read(
                "FiatStakeholderAccounts",
                KeySet.singleKey(
                    Key.of(
                        fiatStakeholderAccountId.userId,
                        fiatStakeholderAccountId.profileId,
                        fiatStakeholderAccountId.value
                    )
                ),
                listOf("Balance")
            ).use {
                if (!it.next()) {
                    "$fiatStakeholderAccountId not found".left().bind<Unit>()
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
                        .newUpdateBuilder("FiatStakeholderAccounts")
                        .set("UserId").to(fiatStakeholderAccountId.userId)
                        .set("ProfileId").to(fiatStakeholderAccountId.profileId)
                        .set("FiatStakeholderAccountId").to(fiatStakeholderAccountId.value)
                        .set("Balance").to(newBalance)
                        .set("UpdatedOn").to(timestamp)
                        .build(),
                    Mutation
                        .newInsertBuilder("FiatStakeholderAccountOperations")
                        .set("UserId").to(fiatStakeholderAccountId.userId)
                        .set("ProfileId").to(fiatStakeholderAccountId.profileId)
                        .set("FiatStakeholderAccountId").to(fiatStakeholderAccountId.value)
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
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        offset: Instant,
        limit: ULong
    ): Either<String, List<AccountOperation>> {
        return queryStatement(
            Statement.newBuilder("""
                SELECT *
                FROM FiatStakeholderAccountOperations
                WHERE UserId = @userId
                AND ProfileId = @profileId
                AND FiatStakeholderAccountId = @accountId
                AND CreatedOn > @offset
                LIMIT @limit
                """.trimIndent()
            )
                .bind("userId").to(fiatStakeholderAccountId.userId)
                .bind("profileId").to(fiatStakeholderAccountId.profileId)
                .bind("accountId").to(fiatStakeholderAccountId.value)
                .bind("offset").to(Timestamp.ofTimeSecondsAndNanos(offset.epochSecond, offset.nano))
                .bind("limit").to(limit.toLong())
                .build()
        ) { resultSet ->
            readAccountOperations(resultSet).right()
        }
    }
}