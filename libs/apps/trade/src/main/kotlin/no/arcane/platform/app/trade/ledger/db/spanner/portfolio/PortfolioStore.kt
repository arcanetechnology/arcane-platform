package no.arcane.platform.app.trade.ledger.db.spanner.portfolio

import arrow.core.Either
import com.google.cloud.Timestamp
import com.google.cloud.spanner.Mutation
import com.google.cloud.spanner.ResultSet
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.Portfolio
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioId
import no.arcane.platform.app.trade.ledger.db.spanner.Table
import no.arcane.platform.app.trade.ledger.db.spanner.toInstant
import no.arcane.platform.app.trade.ledger.db.spanner.insertMutation
import no.arcane.platform.app.trade.ledger.db.spanner.updateMutation

object PortfolioStore {

    private val PortfoliosTable = object : Table<Portfolio> {
        override val name: String = "Profiles"
        override val columns: List<String> = listOf(
            "UserId",
            "ProfileId",
            "FiatStakeholderAccountId",
            "PortfolioId",
            "Alias",
            "CreatedOn",
            "UpdatedOn",
        )

        override fun ResultSet.toObject() = Portfolio(
            portfolioId = PortfolioId(
                userId = getString("UserId"),
                profileId = getString("ProfileId"),
                accountId = getString("FiatStakeholderAccountId"),
                value = getString("PortfolioId"),
            ),
            alias = getString("Alias"),
            createdOn = getTimestamp("CreatedOn").toInstant(),
            updatedOn = getTimestamp("UpdatedOn").toInstant(),
        )
    }

    suspend fun add(
        portfolioId: PortfolioId,
        alias: String,
    ): Either<String, Unit> {
        return insertMutation(
            Mutation.newInsertBuilder("Portfolios")
                .set("UserId").to(portfolioId.userId)
                .set("ProfileId").to(portfolioId.profileId)
                .set("FiatStakeholderAccountId").to(portfolioId.accountId)
                .set("PortfolioId").to(portfolioId.value)
                .set("Alias").to(alias)
                .set("CreatedOn").to(Timestamp.now())
                .set("UpdatedOn").to(Timestamp.now())
                .build(),
            error = "Create Portfolio failed"
        )
    }

    suspend fun update(
        portfolioId: PortfolioId,
        alias: String,
    ): Either<String, Unit> {
        return updateMutation(
            Mutation.newUpdateBuilder("Portfolios")
                .set("UserId").to(portfolioId.userId)
                .set("ProfileId").to(portfolioId.profileId)
                .set("FiatStakeholderAccountId").to(portfolioId.accountId)
                .set("PortfolioId").to(portfolioId.value)
                .set("Alias").to(alias)
                .set("UpdatedOn").to(Timestamp.now())
                .build(),
            error = "Update Portfolio failed"
        )
    }

    suspend fun get(
        portfolioId: PortfolioId,
    ): Either<String, Portfolio> = PortfoliosTable.get(
        portfolioId.userId,
        portfolioId.profileId,
        portfolioId.accountId,
        portfolioId.value,
    )

    suspend fun getAll(
        fiatStakeholderAccountId: FiatStakeholderAccountId,
    ): Either<String, List<Portfolio>> = PortfoliosTable.getAll(
        fiatStakeholderAccountId.userId,
        fiatStakeholderAccountId.profileId,
        fiatStakeholderAccountId.value,
    )
}