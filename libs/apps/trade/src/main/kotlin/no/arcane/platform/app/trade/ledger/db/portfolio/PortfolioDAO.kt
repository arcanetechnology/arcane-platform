package no.arcane.platform.app.trade.ledger.db.portfolio

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import no.arcane.platform.user.UserId
import no.arcane.platform.app.trade.ledger.FiatStakeholderAccountId
import no.arcane.platform.app.trade.ledger.Portfolio
import no.arcane.platform.app.trade.ledger.PortfolioId
import no.arcane.platform.app.trade.ledger.db.selectStatement
import no.arcane.platform.app.trade.ledger.db.updateStatement
import no.arcane.platform.app.trade.ledger.db.usingConnection
import no.arcane.platform.app.trade.ledger.db.validate
import java.sql.ResultSet
import java.time.Instant
import java.util.*
import javax.sql.DataSource

class PortfolioDAO(
    private val dataSource: DataSource
) {
    suspend fun addPortfolio(
        userId: UserId,
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        portfolioId: PortfolioId,
        alias: String,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            either {
                validate(
                    """
                    select fiat_stakeholder_accounts.*
                       from fiat_stakeholder_accounts
                       inner join profiles on fiat_stakeholder_accounts.profile_id = profiles.profile_id
                    where fiat_stakeholder_accounts.fiat_stakeholder_account_id = ?
                        and profiles.user_id = ?
                    """.trimIndent(), error = "Account not found"
                ) {
                    setObject(1, fiatStakeholderAccountId.value)
                    setString(2, userId.value)
                }.bind()
                updateStatement(
                    """
                    insert into portfolios(
                        portfolio_id, 
                        alias, 
                        fiat_stakeholder_account_id,
                        created_on,
                        updated_on
                    ) values (
                        ?,
                        ?,
                        ?,
                        now(),
                        now()
                    ) 
                    """.trimIndent()
                ) {
                    setObject(1, portfolioId.value)
                    setString(2, alias)
                    setObject(3, fiatStakeholderAccountId.value)
                }.bind()
            }
        }
    }

    suspend fun updatePortfolio(
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        portfolioId: PortfolioId,
        alias: String,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            updateStatement(
                """
                update portfolios
                    set alias = ?, updated_on = now()
                where portfolio_id = ?
                    and fiat_stakeholder_account_id = ?
                """.trimIndent()
            ) {
                setString(1, alias)
                setObject(2, portfolioId.value)
                setObject(3, fiatStakeholderAccountId.value)
            }
        }
    }

    suspend fun fetchPortfolio(
        userId: UserId,
        portfolioId: PortfolioId,
    ): Either<String, Portfolio> {
        return dataSource.usingConnection {
            either {
                val resultSet = selectStatement(
                    """
                    select portfolios.*
                        from portfolios
                        inner join fiat_stakeholder_accounts on portfolios.fiat_stakeholder_account_id = fiat_stakeholder_accounts.fiat_stakeholder_account_id 
                        inner join profiles on fiat_stakeholder_accounts.profile_id = profiles.profile_id 
                    where portfolio_id = ?
                        and profiles.user_id = ?
                    """.trimIndent()
                ) {
                    setObject(1, portfolioId.value)
                    setString(2, userId.value)
                }.bind()
                if (resultSet.next()) {
                    resultSet.toPortfolio().right()
                } else {
                    "Portfolio not found".left()
                }.bind()
            }
        }
    }

    suspend fun fetchPortfolios(
        userId: UserId,
    ): Either<String, List<Portfolio>> {
        return dataSource.usingConnection {
            either {
                val resultSet = selectStatement(
                    """
                    select portfolios.*
                        from portfolios
                        inner join fiat_stakeholder_accounts on portfolios.fiat_stakeholder_account_id = fiat_stakeholder_accounts.fiat_stakeholder_account_id 
                        inner join profiles on fiat_stakeholder_accounts.profile_id = profiles.profile_id 
                    where profiles.user_id = ?
                    """.trimIndent()
                ) {
                    setString(1, userId.value)
                }.bind()
                if (!resultSet.next()) {
                    "Portfolios not found".left()
                } else {
                    val portfolios = mutableListOf<Portfolio>()
                    do {
                        portfolios += resultSet.toPortfolio()
                    } while (resultSet.next())
                    portfolios.right()
                }.bind()
            }
        }
    }

    private fun ResultSet.toPortfolio() = Portfolio(
        portfolioId = PortfolioId(getObject("portfolio_id") as UUID),
        alias = getString("alias"),
        accountId = FiatStakeholderAccountId(getObject("fiat_stakeholder_account_id") as UUID),
        createdOn = Instant.ofEpochMilli(getTimestamp("created_on").time),
        updatedOn = Instant.ofEpochMilli(getTimestamp("updated_on").time),
    )
}