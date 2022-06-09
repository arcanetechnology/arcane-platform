package no.arcane.platform.app.trade.ledger.db.spanner

import no.arcane.platform.apps.trade.test.setupSpannerWithTestData
import org.testcontainers.containers.SpannerEmulatorContainer
import org.testcontainers.utility.DockerImageName
import java.io.File

/*class LedgerTests : StringSpec({

    setupSpannerEmulator()

    runBlocking {
        var fiatCustodyAccountsMap: Map<Currency, FiatCustodyAccountId>
        var cryptoCustodyAccountsMap: Map<CryptoCurrency, List<CryptoCustodyAccountId>>
        either {
            kotlin.run {

                // register fiat currency custody accounts
                fiatCustodyAccountsMap = FiatCustodyAccountStore
                    .getAll()
                    .bind()
                    .associate { fiatCustodyAccount ->
                        fiatCustodyAccount.currency to fiatCustodyAccount.fiatCustodyAccountId
                    }
                // register cryptocurrency custody accounts
                cryptoCustodyAccountsMap = CryptoCustodyAccountStore
                    .getAll()
                    .bind()
                    .groupBy { it.cryptoCurrency }
                    .mapValues { (_, accounts) -> accounts.map { it.cryptoCustodyAccountId } }

                listOf("ETH")
                    .map(::CryptoCurrency)
                    .associateWith { cryptoCurrency ->
                        val cryptoCustodyAccountId = CryptoCustodyAccountId("real-eth-coinbase")
                        CryptoCustodyAccountStore.add(
                            cryptoCustodyAccountId = cryptoCustodyAccountId,
                            cryptoCurrency = cryptoCurrency,
                            alias = "Arcane ${cryptoCurrency.value} account",
                        ).bind()
                        cryptoCustodyAccountId
                    }
            }

            // test user
            val userId = UserId(UUID.randomUUID().toString())
            "register a user" {
                Ledger.registerUser(userId).bind().userId shouldBe userId
            }

            var profileId: ProfileId? = null
            "register a profile" {
                val profile = Ledger.addProfile(
                    userId = userId,
                    alias = "Test user profile",
                    type = ProfileType.PERSONAL,
                ).bind()
                profile.alias shouldBe "Test user profile"
                profile.type shouldBe ProfileType.PERSONAL
                profileId = profile.profileId
            }

            var accountId: FiatStakeholderAccountId? = null

            "register accounts" {
                val account: FiatStakeholderAccount = Ledger.addAccount(
                    fiatCustodyAccountId = fiatCustodyAccountsMap[Currency.NOK]!!,
                    currency = Currency.NOK,
                    alias = "Test account",
                    profileId = profileId!!,
                ).bind()
                account.alias shouldBe "Test account"
                account.balance shouldBe 0L
                account.reservedBalance shouldBe 0L
                account.currency shouldBe Currency.NOK
                accountId = account.fiatStakeholderAccountId
            }

            "register crypto accounts" {
                val cryptoAccount = Ledger.addCryptoAccount(
                    cryptoCustodyAccountId = cryptoCustodyAccountsMap[CryptoCurrency("ETH")]!!.first(),
                    profileId = profileId!!,
                    cryptoCurrency = CryptoCurrency("ETH"),
                    alias = "Test crypto account",
                ).bind()
                cryptoAccount.alias shouldBe "Test crypto account"
                cryptoAccount.balance shouldBe 0L
                cryptoAccount.reservedBalance shouldBe 0L
                cryptoAccount.cryptoCurrency shouldBe CryptoCurrency("ETH")
            }

            var portfolioId: PortfolioId? = null

            "register portfolio" {
                val portfolio = Ledger.addPortfolio(
                    accountId = accountId!!,
                    alias = "NOK test portfolio"
                ).bind()
                portfolio.alias shouldBe "NOK test portfolio"
                portfolioId = portfolio.portfolioId
            }

            "register portfolio crypto accounts" {
                val cryptoAccount = Ledger.addPortfolioCryptoAccount(
                    cryptoCustodyAccountId = cryptoCustodyAccountsMap[CryptoCurrency("ETH")]!!.first(),
                    cryptoCurrency = CryptoCurrency("ETH"),
                    alias = "Test crypto account",
                    portfolioId = portfolioId!!
                ).bind()
                cryptoAccount.alias shouldBe "Test crypto account"
                cryptoAccount.balance shouldBe 0L
                cryptoAccount.reservedBalance shouldBe 0L
                cryptoAccount.cryptoCurrency shouldBe CryptoCurrency("ETH")
            }
        }
    }
})*/

fun setupSpannerEmulator() {
    if (System.getenv("SPANNER_EMULATOR_HOST") == null) {
        throw Exception("SPANNER_EMULATOR_HOST env variable is not set.")
    }
    if (System.getenv("GOOGLE_CLOUD_PROJECT") == null) {
        throw Exception("GOOGLE_CLOUD_PROJECT env variable is not set.")
    }

    val emulator = SpannerEmulatorContainer(
        DockerImageName.parse("gcr.io/cloud-spanner-emulator/emulator:latest")
    )
    emulator.portBindings = listOf("9010:9010", "9020:9020")
    emulator.start()

    setupSpannerWithTestData(ddlFile = File("src/main/resources/schema.ddl"))
}