package no.arcane.platform.app.trade.ledger.db.account

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import no.arcane.platform.user.UserId
import no.arcane.platform.app.trade.ledger.CryptoCurrency
import no.arcane.platform.app.trade.ledger.CryptoCustodyAccountId
import no.arcane.platform.app.trade.ledger.CryptoStakeholderAccount
import no.arcane.platform.app.trade.ledger.CryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.PortfolioId
import no.arcane.platform.app.trade.ledger.db.selectStatement
import no.arcane.platform.app.trade.ledger.db.updateStatement
import no.arcane.platform.app.trade.ledger.db.usingConnection
import no.arcane.platform.app.trade.ledger.db.validate
import java.sql.ResultSet
import java.time.Instant
import java.util.*
import javax.sql.DataSource

class CryptoStakeholderAccountDAO(
    private val dataSource: DataSource
) {

    suspend fun addCryptoStakeholderAccount(
        userId: UserId,
        portfolioId: PortfolioId,
        cryptoCustodyAccountId: CryptoCustodyAccountId,
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
        cryptoCurrency: CryptoCurrency,
        alias: String,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            either {
                validate(
                    """
                    select portfolios.*
                        from portfolios
                        inner join fiat_stakeholder_accounts on portfolios.fiat_stakeholder_account_id = fiat_stakeholder_accounts.fiat_stakeholder_account_id
                        inner join profiles on fiat_stakeholder_accounts.profile_id = profiles.profile_id
                    where portfolios.portfolio_id = ?
                        and profiles.user_id = ?
                    """.trimIndent(), error = "Portfolio not found"
                ) {
                    setObject(1, portfolioId.value)
                    setString(2, userId.value)
                }.bind()
                updateStatement(
                    """
                    insert into crypto_stakeholder_accounts(
                        crypto_stakeholder_account_id,
                        balance,
                        reserved_balance,
                        crypto_currency,
                        alias,
                        crypto_custody_account_id,
                        portfolio_id, 
                        created_on, 
                        updated_on
                    ) values (
                        ?,
                        0,
                        0,
                        ?,
                        ?,
                        ?,
                        ?,
                        now(),
                        now()
                    );
                    """.trimIndent()
                ) {
                    setObject(1, cryptoStakeholderAccountId.value)
                    setString(2, cryptoCurrency.value)
                    setString(3, alias)
                    setObject(4, cryptoCustodyAccountId.value)
                    setObject(5, portfolioId.value)
                }.bind()
            }
        }
    }

    suspend fun fetchCryptoStakeholderAccount(
        userId: UserId,
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
    ): Either<String, CryptoStakeholderAccount> {
        return dataSource.usingConnection {
            either {
                val resultSet = selectStatement(
                    """
                    select crypto_stakeholder_accounts.* 
                        from crypto_stakeholder_accounts
                        inner join portfolios on crypto_stakeholder_accounts.portfolio_id = portfolios.portfolio_id
                        inner join fiat_stakeholder_accounts on portfolios.fiat_stakeholder_account_id = fiat_stakeholder_accounts.fiat_stakeholder_account_id
                        inner join profiles on fiat_stakeholder_accounts.profile_id = profiles.profile_id
                    where
                        crypto_stakeholder_accounts.crypto_stakeholder_account_id = ?
                        and profiles.user_id = ?;
                    """.trimIndent()
                ) {
                    setObject(1, cryptoStakeholderAccountId.value)
                    setString(2, userId.value)
                }.bind()
                if (resultSet.next()) {
                    resultSet.toCryptoStakeholderAccount().right()
                } else {
                    "Crypto account not found".left()
                }.bind()
            }
        }
    }

    suspend fun fetchCryptoStakeholderAccounts(
        userId: UserId,
    ): Either<String, List<CryptoStakeholderAccount>> {
        return dataSource.usingConnection {
            either {
                val resultSet = selectStatement(
                    """
                    select crypto_stakeholder_accounts.* 
                        from crypto_stakeholder_accounts
                        inner join portfolios on crypto_stakeholder_accounts.portfolio_id = portfolios.portfolio_id
                        inner join fiat_stakeholder_accounts on portfolios.fiat_stakeholder_account_id = fiat_stakeholder_accounts.fiat_stakeholder_account_id
                        inner join profiles on fiat_stakeholder_accounts.profile_id = profiles.profile_id
                    where
                        profiles.user_id = ?;
                    """.trimIndent()
                ) {
                    setString(1, userId.value)
                }.bind()
                if (!resultSet.next()) {
                    "Crypto account not found".left()
                } else {
                    val cryptoAccounts = mutableListOf<CryptoStakeholderAccount>()
                    do {
                        cryptoAccounts += resultSet.toCryptoStakeholderAccount()
                    } while (resultSet.next())
                    cryptoAccounts.right()
                }.bind()
            }
        }
    }

    private fun ResultSet.toCryptoStakeholderAccount() = CryptoStakeholderAccount(
        cryptoStakeholderAccountId = CryptoStakeholderAccountId(getObject("crypto_stakeholder_account_id") as UUID),
        portfolioId = PortfolioId(getObject("portfolio_id") as UUID),
        balance = getBigDecimal("balance").toBigInteger(),
        reservedBalance = getBigDecimal("reserved_balance").toBigInteger(),
        cryptoCurrency = CryptoCurrency(getString("crypto_currency")),
        alias = getString("alias"),
        createdOn = Instant.ofEpochMilli(getTimestamp("created_on").time),
        updatedOn = Instant.ofEpochMilli(getTimestamp("updated_on").time),
    )
}