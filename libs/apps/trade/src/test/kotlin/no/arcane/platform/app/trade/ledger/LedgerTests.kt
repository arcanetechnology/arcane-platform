package no.arcane.platform.app.trade.ledger

import arrow.core.continuations.either
import io.kotest.common.runBlocking
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.testcontainers.JdbcTestContainerExtension
import io.kotest.matchers.shouldBe
import no.arcane.platform.user.UserId
import no.arcane.platform.app.trade.ledger.Ledger.addAccount
import no.arcane.platform.app.trade.ledger.Ledger.addCryptoAccount
import no.arcane.platform.app.trade.ledger.Ledger.addPortfolio
import no.arcane.platform.app.trade.ledger.Ledger.addProfile
import no.arcane.platform.app.trade.ledger.Ledger.buyCrypto
import no.arcane.platform.app.trade.ledger.Ledger.creditAccount
import no.arcane.platform.app.trade.ledger.Ledger.debitAccount
import no.arcane.platform.app.trade.ledger.Ledger.fetchAccount
import no.arcane.platform.app.trade.ledger.Ledger.fetchCryptoAccount
import no.arcane.platform.app.trade.ledger.Ledger.registerUser
import no.arcane.platform.app.trade.ledger.Ledger.sellCrypto
import no.arcane.platform.app.trade.ledger.db.DataSourceProvider
import no.arcane.platform.app.trade.ledger.db.account.CryptoCustodyAccountDAO
import no.arcane.platform.app.trade.ledger.db.account.FiatCustodyAccountsDAO
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigInteger
import java.util.*

class LedgerTests : StringSpec({

    val postgres = PostgreSQLContainer<Nothing>("postgres:alpine").apply {
        startupAttempts = 1
        withUsername("postgres")
        withPassword("postgres")
        withDatabaseName("trade")
        withInitScript("schema.ddl")
    }

    DataSourceProvider.testDatasource = install(JdbcTestContainerExtension(postgres)) {
        poolName = "test_pool"
        maximumPoolSize = 8
    }

    runBlocking {
        either {
            kotlin.run {
                // setup
                val fiatCustodyAccountsDAO = FiatCustodyAccountsDAO(DataSourceProvider.getDataSource())
                val cryptoCustodyAccountDAO = CryptoCustodyAccountDAO(DataSourceProvider.getDataSource())

                // register fiat currency custody accounts
                Currency.values().forEach { currency ->
                    val fiatCustodyAccountId = FiatCustodyAccountId(UUID.randomUUID())
                    fiatCustodyAccountsDAO.addFiatCustodyAccount(
                        fiatCustodyAccountId = fiatCustodyAccountId,
                        alias = "Arcane ${currency.name} account",
                        currency = currency,
                    ).bind()
                }
                // register cryptocurrency custody accounts
                listOf("BTC", "ETH").forEach { cryptoCurrency ->
                    val cryptoCustodyAccountId = CryptoCustodyAccountId(UUID.randomUUID())
                    cryptoCustodyAccountDAO.addCryptoCustodyAccount(
                        cryptoCustodyAccountId = cryptoCustodyAccountId,
                        cryptoCurrency = CryptoCurrency(cryptoCurrency),
                        alias = "Arcane $cryptoCurrency account",
                    ).bind()
                }
            }

            // test user
            val userId = UserId(UUID.randomUUID().toString())
            "register a user" {
                userId.registerUser().bind().userId shouldBe userId
            }

            var profileId: ProfileId? = null
            "register a profile" {
                val profile = userId.addProfile(
                    alias = "Test user profile",
                    type = ProfileType.PERSONAL,
                ).bind()
                profile.alias shouldBe "Test user profile"
                profile.type shouldBe ProfileType.PERSONAL
                profileId = profile.profileId
            }

            var accountId: FiatStakeholderAccountId? = null

            "register accounts" {
                val account: FiatStakeholderAccount = userId.addAccount(
                    currency = Currency.NOK,
                    alias = "Test account",
                    profileId = profileId!!,
                ).bind()
                account.alias shouldBe "Test account"
                account.balance shouldBe BigInteger.ZERO
                account.reservedBalance shouldBe BigInteger.ZERO
                account.currency shouldBe Currency.NOK
                accountId = account.fiatStakeholderAccountId
            }

            var portfolioId: PortfolioId? = null

            "register portfolio" {
                val portfolio = userId.addPortfolio(
                    accountId = accountId!!,
                    alias = "NOK test portfolio"
                ).bind()
                portfolio.accountId shouldBe accountId
                portfolio.alias shouldBe "NOK test portfolio"
                portfolioId = portfolio.portfolioId
            }

            var cryptoAccountId: CryptoStakeholderAccountId? = null

            "register crypto accounts" {
                val cryptoAccount = userId.addCryptoAccount(
                    cryptoCurrency = CryptoCurrency("BTC"),
                    alias = "Test crypto account",
                    portfolioId = portfolioId!!
                ).bind()
                cryptoAccount.alias shouldBe "Test crypto account"
                cryptoAccount.balance shouldBe BigInteger.ZERO
                cryptoAccount.reservedBalance shouldBe BigInteger.ZERO
                cryptoAccount.cryptoCurrency shouldBe CryptoCurrency("BTC")
                cryptoAccount.portfolioId shouldBe portfolioId
                cryptoAccountId = cryptoAccount.cryptoStakeholderAccountId
            }

            "credit account" {
                userId.creditAccount(
                    accountId = accountId!!,
                    amount = Amount(
                        value = BigInteger.valueOf(100),
                        currency = Currency.NOK,
                    )
                ).bind()

                userId.fetchAccount(accountId = accountId!!)
                    .bind()
                    .balance shouldBe BigInteger.valueOf(100)
            }

            "debit account - insufficient balance" {
                userId.debitAccount(
                    accountId = accountId!!,
                    amount = Amount(
                        value = BigInteger.valueOf(120),
                        currency = Currency.NOK,
                    )
                ).isLeft() shouldBe true

                userId.fetchAccount(accountId = accountId!!)
                    .bind()
                    .balance shouldBe BigInteger.valueOf(100)
            }

            "debit account" {
                userId.debitAccount(
                    accountId = accountId!!,
                    amount = Amount(
                        value = BigInteger.valueOf(10),
                        currency = Currency.NOK,
                    )
                ).bind()

                userId.fetchAccount(accountId = accountId!!)
                    .bind()
                    .balance shouldBe BigInteger.valueOf(90)
            }

            "buy crypto" {
                userId.buyCrypto(
                    cryptoAccountId = cryptoAccountId!!,
                    amount = Amount(
                        value = BigInteger.valueOf(20),
                        currency = Currency.NOK,
                    ),
                    crypto = Crypto(
                        value = BigInteger.valueOf(5),
                        currency = CryptoCurrency("BTC"),
                    )
                ).bind()

                userId.fetchAccount(accountId = accountId!!)
                    .bind()
                    .balance shouldBe BigInteger.valueOf(70)
                userId.fetchCryptoAccount(cryptoAccountId = cryptoAccountId!!)
                    .bind()
                    .balance shouldBe BigInteger.valueOf(5)
            }

            "sell crypto" {
                userId.sellCrypto(
                    cryptoAccountId = cryptoAccountId!!,
                    amount = Amount(
                        value = BigInteger.valueOf(15),
                        currency = Currency.NOK,
                    ),
                    crypto = Crypto(
                        value = BigInteger.valueOf(3),
                        currency = CryptoCurrency("BTC"),
                    )
                ).bind()

                userId.fetchAccount(accountId = accountId!!)
                    .bind()
                    .balance shouldBe BigInteger.valueOf(85)
                userId.fetchCryptoAccount(cryptoAccountId = cryptoAccountId!!)
                    .bind()
                    .balance shouldBe BigInteger.valueOf(2)
            }
        }
    }
})