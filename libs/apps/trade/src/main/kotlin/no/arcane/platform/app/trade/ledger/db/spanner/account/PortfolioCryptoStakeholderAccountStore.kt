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
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioCryptoStakeholderAccount
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioCryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioId
import no.arcane.platform.app.trade.ledger.db.spanner.Table
import no.arcane.platform.app.trade.ledger.db.spanner.TransactionId
import no.arcane.platform.app.trade.ledger.db.spanner.insertMutation
import no.arcane.platform.app.trade.ledger.db.spanner.queryStatement
import no.arcane.platform.app.trade.ledger.db.spanner.toInstant
import no.arcane.platform.app.trade.ledger.db.spanner.updateMutation
import java.time.Instant
import java.util.*

object PortfolioCryptoStakeholderAccountStore {

    private val PortfolioCryptoStakeholderAccountsTable = object : Table<PortfolioCryptoStakeholderAccount> {
        override val name: String = "PortfolioCryptoStakeholderAccounts"
        override val columns: List<String> = listOf(
            "UserId",
            "ProfileId",
            "FiatStakeholderAccountId",
            "PortfolioId",
            "PortfolioCryptoStakeholderAccountId",
            "CryptoCustodyAccountId",
            "Balance",
            "ReservedBalance",
            "CryptoCurrency",
            "Alias",
            "CreatedOn",
            "UpdatedOn",
        )

        override fun ResultSet.toObject() = PortfolioCryptoStakeholderAccount(
            id = PortfolioCryptoStakeholderAccountId(
                userId = getString("UserId"),
                profileId = getString("ProfileId"),
                accountId = getString("FiatStakeholderAccountId"),
                portfolioId = getString("PortfolioId"),
                value = getString("PortfolioCryptoStakeholderAccountId"),
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
        portfolioCryptoStakeholderAccountId: PortfolioCryptoStakeholderAccountId,
        cryptoCurrency: CryptoCurrency,
        alias: String,
    ): Either<String, Unit> {
        return insertMutation(
            Mutation.newInsertBuilder("PortfolioCryptoStakeholderAccounts")
                .set("UserId").to(portfolioCryptoStakeholderAccountId.userId)
                .set("ProfileId").to(portfolioCryptoStakeholderAccountId.profileId)
                .set("FiatStakeholderAccountId").to(portfolioCryptoStakeholderAccountId.accountId)
                .set("PortfolioId").to(portfolioCryptoStakeholderAccountId.portfolioId)
                .set("PortfolioCryptoStakeholderAccountId").to(portfolioCryptoStakeholderAccountId.value)
                .set("Balance").to(0L)
                .set("ReservedBalance").to(0L)
                .set("CryptoCurrency").to(cryptoCurrency.toText())
                .set("Alias").to(alias)
                .set("CryptoCustodyAccountId").to(cryptoCustodyAccountId.value)
                .set("CreatedOn").to(Timestamp.now())
                .set("UpdatedOn").to(Timestamp.now())
                .build(),
            error = "Create PortfolioCryptoStakeholderAccount failed"
        )
    }

    suspend fun update(
        portfolioCryptoStakeholderAccountId: PortfolioCryptoStakeholderAccountId,
        alias: String,
    ): Either<String, Unit> {
        return updateMutation(
            Mutation.newUpdateBuilder("CryptoStakeholderAccounts")
                .set("UserId").to(portfolioCryptoStakeholderAccountId.userId)
                .set("ProfileId").to(portfolioCryptoStakeholderAccountId.profileId)
                .set("FiatStakeholderAccountId").to(portfolioCryptoStakeholderAccountId.accountId)
                .set("PortfolioId").to(portfolioCryptoStakeholderAccountId.portfolioId)
                .set("PortfolioCryptoStakeholderAccountId").to(portfolioCryptoStakeholderAccountId.value)
                .set("Alias").to(alias)
                .set("UpdatedOn").to(Timestamp.now())
                .build(),
            error = "Update CryptoStakeholderAccount failed"
        )
    }

    suspend fun get(
        portfolioCryptoStakeholderAccountId: PortfolioCryptoStakeholderAccountId,
    ): Either<String, PortfolioCryptoStakeholderAccount> = PortfolioCryptoStakeholderAccountsTable.get(
        portfolioCryptoStakeholderAccountId.userId,
        portfolioCryptoStakeholderAccountId.profileId,
        portfolioCryptoStakeholderAccountId.accountId,
        portfolioCryptoStakeholderAccountId.portfolioId,
        portfolioCryptoStakeholderAccountId.value,
    )

    suspend fun get(
        transactionContext: TransactionContext,
        portfolioCryptoStakeholderAccountId: PortfolioCryptoStakeholderAccountId,
    ): Either<String, PortfolioCryptoStakeholderAccount> = PortfolioCryptoStakeholderAccountsTable.get(
        readContext = transactionContext,
        portfolioCryptoStakeholderAccountId.userId,
        portfolioCryptoStakeholderAccountId.profileId,
        portfolioCryptoStakeholderAccountId.accountId,
        portfolioCryptoStakeholderAccountId.portfolioId,
        portfolioCryptoStakeholderAccountId.value,
    )

    suspend fun getAll(
        portfolioId: PortfolioId,
    ): Either<String, List<PortfolioCryptoStakeholderAccount>> = PortfolioCryptoStakeholderAccountsTable.getAll(
        portfolioId.userId,
        portfolioId.profileId,
        portfolioId.value,
    )

    suspend fun operate(
        transactionContext: TransactionContext,
        portfolioCryptoStakeholderAccountId: PortfolioCryptoStakeholderAccountId,
        transactionId: TransactionId,
        amount: Long,
        timestamp: Timestamp,
    ): Either<String, Unit> {
        return either {
            val balance = transactionContext.read(
                "PortfolioCryptoStakeholderAccounts",
                KeySet.singleKey(
                    Key.of(
                        portfolioCryptoStakeholderAccountId.userId,
                        portfolioCryptoStakeholderAccountId.profileId,
                        portfolioCryptoStakeholderAccountId.accountId,
                        portfolioCryptoStakeholderAccountId.portfolioId,
                        portfolioCryptoStakeholderAccountId.value
                    )
                ),
                listOf("Balance")
            ).use {
                it.getLong("Balance")
            }
            val newBalance = balance + amount
            if (newBalance < 0) {
                "Insufficient balance".left().bind<Unit>()
            }
            transactionContext.buffer(
                listOf(
                    Mutation
                        .newUpdateBuilder("PortfolioCryptoStakeholderAccounts")
                        .set("UserId").to(portfolioCryptoStakeholderAccountId.userId)
                        .set("ProfileId").to(portfolioCryptoStakeholderAccountId.profileId)
                        .set("FiatStakeholderAccountId").to(portfolioCryptoStakeholderAccountId.accountId)
                        .set("PortfolioId").to(portfolioCryptoStakeholderAccountId.portfolioId)
                        .set("PortfolioCryptoStakeholderAccountId").to(portfolioCryptoStakeholderAccountId.value)
                        .set("Balance").to(newBalance)
                        .set("UpdatedOn").to(timestamp)
                        .build(),
                    Mutation
                        .newInsertBuilder("PortfolioCryptoStakeholderAccountOperations")
                        .set("UserId").to(portfolioCryptoStakeholderAccountId.userId)
                        .set("ProfileId").to(portfolioCryptoStakeholderAccountId.profileId)
                        .set("FiatStakeholderAccountId").to(portfolioCryptoStakeholderAccountId.accountId)
                        .set("PortfolioId").to(portfolioCryptoStakeholderAccountId.portfolioId)
                        .set("PortfolioCryptoStakeholderAccountId").to(portfolioCryptoStakeholderAccountId.value)
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
        portfolioCryptoStakeholderAccountId: PortfolioCryptoStakeholderAccountId,
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
                AND PortfolioId = @portfolioId
                AND PortfolioCryptoStakeholderAccountId = @portfolioAccountId
                AND CreatedOn > @offset
                LIMIT @limit
                """.trimIndent()
            )
                .bind("userId").to(portfolioCryptoStakeholderAccountId.userId)
                .bind("profileId").to(portfolioCryptoStakeholderAccountId.profileId)
                .bind("accountId").to(portfolioCryptoStakeholderAccountId.accountId)
                .bind("portfolioId").to(portfolioCryptoStakeholderAccountId.portfolioId)
                .bind("portfolioAccountId").to(portfolioCryptoStakeholderAccountId.value)
                .bind("offset").to(Timestamp.ofTimeSecondsAndNanos(offset.epochSecond, offset.nano))
                .bind("limit").to(limit.toLong())
                .build()
        ) { resultSet ->
            readAccountOperations(resultSet).right()
        }
    }
}