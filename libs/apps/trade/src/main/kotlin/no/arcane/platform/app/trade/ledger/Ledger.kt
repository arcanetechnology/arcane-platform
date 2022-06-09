package no.arcane.platform.app.trade.ledger

import arrow.core.Either
import arrow.core.continuations.either
import no.arcane.platform.app.trade.ledger.db.DataSourceProvider
import no.arcane.platform.app.trade.ledger.db.TradeDatastore
import no.arcane.platform.app.trade.ledger.db.account.CryptoCustodyAccountDAO
import no.arcane.platform.app.trade.ledger.db.account.CryptoStakeholderAccountDAO
import no.arcane.platform.app.trade.ledger.db.account.FiatCustodyAccountsDAO
import no.arcane.platform.app.trade.ledger.db.account.FiatStakeholderAccountsDAO
import no.arcane.platform.app.trade.ledger.db.portfolio.PortfolioDAO
import no.arcane.platform.app.trade.ledger.db.profile.ProfileDAO
import no.arcane.platform.app.trade.ledger.db.user.UserDAO
import no.arcane.platform.user.UserId
import java.util.*

object Ledger {

    private val dataSource by lazy { DataSourceProvider.getDataSource() }

    private val fiatCustodyAccountsDAO by lazy { FiatCustodyAccountsDAO(dataSource) }
    private val cryptoCustodyAccountsDAO by lazy { CryptoCustodyAccountDAO(dataSource) }

    private val userDAO by lazy { UserDAO(dataSource) }
    private val profileDAO by lazy { ProfileDAO(dataSource) }
    private val fiatStakeholderAccountsDAO by lazy { FiatStakeholderAccountsDAO(dataSource) }
    private val portfolioDAO by lazy { PortfolioDAO(dataSource) }
    private val cryptoStakeholderAccountDAO by lazy { CryptoStakeholderAccountDAO(dataSource) }

    private val tradeDatastore by lazy { TradeDatastore(dataSource) }

    suspend fun UserId.registerUser(): Either<String, User> {
        return either {
            userDAO.createUser(userId = this@registerUser).bind()
            userDAO.fetchUser(userId = this@registerUser).bind()
        }
    }

    suspend fun UserId.addProfile(
        alias: String,
        type: ProfileType
    ): Either<String, Profile> {
        return either {
            val profileId = ProfileId(UUID.randomUUID())
            profileDAO.addProfile(
                userId = this@addProfile,
                profileId = profileId,
                alias = alias,
                type = type
            ).bind()
            profileDAO.fetchProfile(
                userId = this@addProfile,
                profileId = profileId
            ).bind()
        }
    }

    suspend fun UserId.addAccount(
        profileId: ProfileId,
        currency: Currency,
        alias: String,
    ): Either<String, FiatStakeholderAccount> {
        val fiatStakeholderAccountId = FiatStakeholderAccountId(UUID.randomUUID())
        return either {
            val fiatCustodyAccountId = fiatCustodyAccountsDAO.fetchFiatCustodyAccount(
                currency = currency
            ).bind().fiatCustodyAccountId
            fiatStakeholderAccountsDAO.addFiatStakeholderAccount(
                userId = this@addAccount,
                fiatStakeholderAccountId = fiatStakeholderAccountId,
                currency = currency,
                alias = alias,
                profileId = profileId,
                fiatCustodyAccountId = fiatCustodyAccountId,
            ).bind()
            fiatStakeholderAccountsDAO.fetchFiatStakeholderAccount(
                userId = this@addAccount,
                fiatStakeholderAccountId = fiatStakeholderAccountId,
            ).bind()
        }
    }

    suspend fun UserId.fetchAccount(
        accountId: FiatStakeholderAccountId
    ): Either<String, FiatStakeholderAccount> {
        return fiatStakeholderAccountsDAO.fetchFiatStakeholderAccount(
            userId = this,
            fiatStakeholderAccountId = accountId,
        )
    }

    suspend fun UserId.addPortfolio(
        accountId: FiatStakeholderAccountId,
        alias: String,
    ): Either<String, Portfolio> {
        val portfolioId = PortfolioId(UUID.randomUUID())
        return either {
            portfolioDAO.addPortfolio(
                userId = this@addPortfolio,
                fiatStakeholderAccountId = accountId,
                portfolioId = portfolioId,
                alias = alias,
            ).bind()
            portfolioDAO.fetchPortfolio(
                userId = this@addPortfolio,
                portfolioId = portfolioId,
            ).bind()
        }
    }

    suspend fun UserId.addCryptoAccount(
        cryptoCurrency: CryptoCurrency,
        alias: String,
        portfolioId: PortfolioId,
    ): Either<String, CryptoStakeholderAccount> {
        val cryptoStakeholderAccountId = CryptoStakeholderAccountId(UUID.randomUUID())
        return either {
            val cryptoCustodyAccountId = cryptoCustodyAccountsDAO.fetchCryptoCustodyAccount(
                cryptoCurrency = cryptoCurrency,
            ).bind().cryptoCustodyAccountId
            cryptoStakeholderAccountDAO.addCryptoStakeholderAccount(
                userId = this@addCryptoAccount,
                portfolioId = portfolioId,
                cryptoCustodyAccountId = cryptoCustodyAccountId,
                cryptoStakeholderAccountId = cryptoStakeholderAccountId,
                cryptoCurrency = cryptoCurrency,
                alias = alias,
            ).bind()
            cryptoStakeholderAccountDAO.fetchCryptoStakeholderAccount(
                userId = this@addCryptoAccount,
                cryptoStakeholderAccountId = cryptoStakeholderAccountId,
            ).bind()
        }
    }

    suspend fun UserId.fetchCryptoAccount(
        cryptoAccountId: CryptoStakeholderAccountId
    ): Either<String, CryptoStakeholderAccount> = cryptoStakeholderAccountDAO.fetchCryptoStakeholderAccount(
        userId = this,
        cryptoStakeholderAccountId = cryptoAccountId,
    )

    suspend fun UserId.creditAccount(
        accountId: FiatStakeholderAccountId,
        amount: Amount,
    ): Either<String, Unit> {
        return tradeDatastore.creditAccount(
            userId = this,
            fiatStakeholderAccountId = accountId,
            amount = amount
        )
    }

    suspend fun UserId.debitAccount(
        accountId: FiatStakeholderAccountId,
        amount: Amount,
    ): Either<String, Unit> {
        return tradeDatastore.debitAccount(
            userId = this,
            fiatStakeholderAccountId = accountId,
            amount = amount
        )
    }

    suspend fun UserId.buyCrypto(
        cryptoAccountId: CryptoStakeholderAccountId,
        amount: Amount,
        crypto: Crypto,
    ): Either<String, Unit> {
        return tradeDatastore.creditCryptoAccount(
            userId = this,
            cryptoStakeholderAccountId = cryptoAccountId,
            amount = amount,
            crypto = crypto,
        )
    }

    suspend fun UserId.sellCrypto(
        cryptoAccountId: CryptoStakeholderAccountId,
        amount: Amount,
        crypto: Crypto,
    ): Either<String, Unit> {
        return tradeDatastore.debitCryptoAccount(
            userId = this,
            cryptoStakeholderAccountId = cryptoAccountId,
            amount = amount,
            crypto = crypto,
        )
    }
}