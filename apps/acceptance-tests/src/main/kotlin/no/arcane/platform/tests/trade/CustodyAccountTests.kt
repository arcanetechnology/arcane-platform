package no.arcane.platform.tests.trade

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import kotlinx.serialization.Serializable

@Serializable
data class CustodyAccount(
    val id: String,
    val balance: Long = 0,
    val reservedBalance: Long = 0,
    val currency: String,
    val alias: String,
    val createdOn: String,
    val updatedOn: String,
)


suspend fun getCustodyAccounts(): List<CustodyAccount> =
    get(path = "apps/trade-admin/custody-accounts").body()

suspend fun getCustodyAccount(accountId: String): CustodyAccount =
    get(path = "apps/trade-admin/custody-accounts/${accountId}").body()

fun BehaviorSpec.custodyAccountTests() {
    given("Custody accounts are provisioned") {
        `when`("GET /apps/trade-admin/custody-accounts") {
            val custodyAccounts = getCustodyAccounts()
            then("Should return list of custody accounts") {
                custodyAccounts.map { it.copy(createdOn = "", updatedOn = "") } shouldContainExactlyInAnyOrder listOf(
                    CustodyAccount("real-chf-sp1", 1000000, 0, "CHF", "Real CHF SP1", "", ""),
                    CustodyAccount("real-dkk-sp1", 1000000, 0, "DKK", "Real DKK SP1", "", ""),
                    CustodyAccount("real-eur-sp1", 1000000, 0, "EUR", "Real EUR SP1", "", ""),
                    CustodyAccount("real-gbp-sp1", 1000000, 0, "GBP", "Real GBP SP1", "", ""),
                    CustodyAccount("real-nok-sp1", 1000000, 0, "NOK", "Real NOK SP1", "", ""),
                    CustodyAccount("real-sek-sp1", 1000000, 0, "SEK", "Real SEK SP1", "", ""),
                    CustodyAccount("real-usd-sp1", 1000000, 0, "USD", "Real USD SP1", "", ""),
                )
            }
        }
        `when`("GET /apps/trade-admin/custody-accounts/{account-id}") {
            val custodyAccount = getCustodyAccount("real-nok-sp1")
            then("Should return custody account") {
                custodyAccount.copy(createdOn = "", updatedOn = "") shouldBe
                        CustodyAccount("real-nok-sp1", 1000000, 0, "NOK", "Real NOK SP1", "", "")
            }
        }
    }
}

suspend fun getCryptoCustodyAccounts(): List<CustodyAccount> =
    get(path = "apps/trade-admin/crypto-custody-accounts").body()

suspend fun getCryptoCustodyAccount(accountId: String): CustodyAccount =
    get(path = "apps/trade-admin/crypto-custody-accounts/${accountId}").body()

fun BehaviorSpec.cryptoCustodyAccountTests() {
    given("Crypto custody accounts are provisioned") {
        `when`("GET /apps/trade-admin/crypto-custody-accounts") {
            val cryptoCustodyAccounts = getCryptoCustodyAccounts()
            then("Should return list of crypto custody accounts") {
                cryptoCustodyAccounts.map { it.copy(createdOn = "", updatedOn = "") } shouldContainExactlyInAnyOrder listOf(
                    CustodyAccount("real-eth-coinbase", 1000000, 0, "ETH", "Real ETH Coinbase", "", ""),
                    CustodyAccount("real-eth-metamask", 1000000, 0, "ETH", "Real ETH Metamask", "", ""),
                    CustodyAccount("real-matic-ftx", 1000000, 0, "MATIC", "Real MATIC FTX", "", ""),
                    CustodyAccount("real-matic-metamask", 1000000, 0, "MATIC", "Real MATIC Metamask", "", ""),
                )
            }
        }
        `when`("GET /apps/trade-admin/crypto-custody-accounts/{account-id}") {
            val custodyAccount = getCryptoCustodyAccount("real-eth-coinbase")
            then("Should return custody account") {
                custodyAccount.copy(createdOn = "", updatedOn = "") shouldBe
                        CustodyAccount("real-eth-coinbase", 1000000, 0, "ETH", "Real ETH Coinbase", "", "")
            }
        }
    }
}