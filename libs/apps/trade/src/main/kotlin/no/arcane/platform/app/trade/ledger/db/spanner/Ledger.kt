package no.arcane.platform.app.trade.ledger.db.spanner

import arrow.core.Either
import arrow.core.continuations.either
import no.arcane.platform.app.trade.admin.api.rest.AddOperation
import no.arcane.platform.app.trade.admin.api.rest.Transaction
import no.arcane.platform.app.trade.ledger.db.spanner.account.CryptoStakeholderAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.account.FiatStakeholderAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.account.PortfolioCryptoStakeholderAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.portfolio.PortfolioStore
import no.arcane.platform.app.trade.ledger.db.spanner.profile.ProfileStore
import no.arcane.platform.app.trade.ledger.db.spanner.transaction.TransactionStore
import no.arcane.platform.app.trade.ledger.db.spanner.user.UserStore
import no.arcane.platform.user.UserId
import java.util.*

object Ledger {

    suspend fun registerUser(
        userId: UserId,
    ): Either<String, User> {
        return either {
            UserStore.add(userId = userId).bind()
            UserStore.get(userId = userId).bind()
        }
    }

    suspend fun addProfile(
        userId: UserId,
        alias: String,
        type: ProfileType
    ): Either<String, Profile> {
        return either {
            val profileId = ProfileId(
                userId = userId.value,
                value = UUID.randomUUID().toString(),
            )
            ProfileStore.add(
                profileId = profileId,
                alias = alias,
                type = type
            ).bind()
            ProfileStore.get(
                profileId = profileId
            ).bind()
        }
    }

    suspend fun updateProfile(
        profileId: ProfileId,
        alias: String,
    ): Either<String, Profile> {
        return either {
            ProfileStore.update(
                profileId = profileId,
                alias = alias,
            ).bind()
            ProfileStore.get(
                profileId = profileId
            ).bind()
        }
    }

    suspend fun addAccount(
        fiatCustodyAccountId: FiatCustodyAccountId,
        profileId: ProfileId,
        currency: FiatCurrency,
        alias: String,
    ): Either<String, FiatStakeholderAccount> {
        val fiatStakeholderAccountId = FiatStakeholderAccountId(
            userId = profileId.userId,
            profileId = profileId.value,
            value = UUID.randomUUID().toString()
        )
        return either {
            FiatStakeholderAccountStore.add(
                fiatStakeholderAccountId = fiatStakeholderAccountId,
                currency = currency,
                alias = alias,
                fiatCustodyAccountId = fiatCustodyAccountId,
            ).bind()
            FiatStakeholderAccountStore.get(
                fiatStakeholderAccountId = fiatStakeholderAccountId,
            ).bind()
        }
    }

    suspend fun updateAccount(
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        alias: String,
    ): Either<String, FiatStakeholderAccount> {
        return either {
            FiatStakeholderAccountStore.update(
                fiatStakeholderAccountId = fiatStakeholderAccountId,
                alias = alias,
            ).bind()
            FiatStakeholderAccountStore.get(
                fiatStakeholderAccountId = fiatStakeholderAccountId,
            ).bind()
        }
    }

    suspend fun addCryptoAccount(
        cryptoCustodyAccountId: CryptoCustodyAccountId,
        profileId: ProfileId,
        cryptoCurrency: CryptoCurrency,
        alias: String,
    ): Either<String, CryptoStakeholderAccount> {
        val cryptoStakeholderAccountId = CryptoStakeholderAccountId(
            userId = profileId.userId,
            profileId = profileId.value,
            value = UUID.randomUUID().toString(),
        )
        return either {
            CryptoStakeholderAccountStore.add(
                cryptoCustodyAccountId = cryptoCustodyAccountId,
                cryptoStakeholderAccountId = cryptoStakeholderAccountId,
                cryptoCurrency = cryptoCurrency,
                alias = alias,
            ).bind()
            CryptoStakeholderAccountStore.get(
                cryptoStakeholderAccountId = cryptoStakeholderAccountId,
            ).bind()
        }
    }

    suspend fun updateCryptoAccount(
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
        alias: String,
    ): Either<String, CryptoStakeholderAccount> {
        return either {
            CryptoStakeholderAccountStore.update(
                cryptoStakeholderAccountId = cryptoStakeholderAccountId,
                alias = alias,
            ).bind()
            CryptoStakeholderAccountStore.get(
                cryptoStakeholderAccountId = cryptoStakeholderAccountId,
            ).bind()
        }
    }

    suspend fun addPortfolio(
        accountId: FiatStakeholderAccountId,
        alias: String,
    ): Either<String, Portfolio> {
        val portfolioId = PortfolioId(
            userId = accountId.userId,
            profileId = accountId.profileId,
            accountId = accountId.value,
            value = UUID.randomUUID().toString(),
        )
        return either {
            PortfolioStore.add(
                portfolioId = portfolioId,
                alias = alias,
            ).bind()
            PortfolioStore.get(
                portfolioId = portfolioId,
            ).bind()
        }
    }

    suspend fun updatePortfolio(
        portfolioId: PortfolioId,
        alias: String,
    ): Either<String, Portfolio> {
        return either {
            PortfolioStore.update(
                portfolioId = portfolioId,
                alias = alias,
            ).bind()
            PortfolioStore.get(
                portfolioId = portfolioId,
            ).bind()
        }
    }

    suspend fun addPortfolioCryptoAccount(
        cryptoCustodyAccountId: CryptoCustodyAccountId,
        portfolioId: PortfolioId,
        cryptoCurrency: CryptoCurrency,
        alias: String,
    ): Either<String, PortfolioCryptoStakeholderAccount> {
        val portfolioCryptoStakeholderAccountId = PortfolioCryptoStakeholderAccountId(
            userId = portfolioId.userId,
            profileId = portfolioId.profileId,
            accountId = portfolioId.accountId,
            portfolioId = portfolioId.value,
            value = UUID.randomUUID().toString())
        return either {
            PortfolioCryptoStakeholderAccountStore.add(
                cryptoCustodyAccountId = cryptoCustodyAccountId,
                cryptoCurrency = cryptoCurrency,
                portfolioCryptoStakeholderAccountId = portfolioCryptoStakeholderAccountId,
                alias = alias,
            ).bind()
            PortfolioCryptoStakeholderAccountStore.get(
                portfolioCryptoStakeholderAccountId = portfolioCryptoStakeholderAccountId,
            ).bind()
        }
    }

    suspend fun updatePortfolioCryptoAccount(
        portfolioCryptoStakeholderAccountId: PortfolioCryptoStakeholderAccountId,
        alias: String,
    ): Either<String, PortfolioCryptoStakeholderAccount> {
        return either {
            PortfolioCryptoStakeholderAccountStore.update(
                portfolioCryptoStakeholderAccountId = portfolioCryptoStakeholderAccountId,
                alias = alias,
            ).bind()
            PortfolioCryptoStakeholderAccountStore.get(
                portfolioCryptoStakeholderAccountId = portfolioCryptoStakeholderAccountId,
            ).bind()
        }
    }

    suspend fun addTransaction(
        operations: List<AddOperation>
    ): Either<List<String>, Transaction> {
        val txnId = TransactionId(UUID.randomUUID().toString())
        return TransactionStore
            .add(
                transactionId = txnId,
                operations = operations,
            )
    }
}