package no.arcane.platform.tests.trade

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*

fun BehaviorSpec.transactionTests() {

    given("user has fiat and crypto stakeholder accounts") {
        val userId = UUID.randomUUID().toString()
        registerUser(userId = userId)
        val profileId = addProfile(
            userId = userId,
            addProfile = AddProfile(
                alias = "Test Profile",
                type = ProfileType.PERSONAL,
            )
        ).id
        val accountId = addAccount(
            userId = userId,
            profileId = profileId,
            addAccount = AddAccount(currency = "NOK", alias = "Test account", custodyAccountId = "real-nok-sp1"),
        ).id
        val cryptoAccountId = addCryptoAccount(
            userId = userId,
            profileId = profileId,
            addAccount = AddAccount(currency = "ETH", alias = "Test crypto account", custodyAccountId = "real-eth-coinbase"),
        ).id
        `when`("POST /apps/trade-admin/transactions") {
            val operations = listOf(
                AddOperation(
                    accountId = "/virtual-accounts/user-external-nok",
                    amount = -1000
                ),
                AddOperation(
                    accountId = "/custody-accounts/real-nok-sp1",
                    amount = 1000
                ),
                AddOperation(
                    accountId = "/users/$userId/profiles/$profileId/accounts/$accountId",
                    amount = 1000
                ),
                AddOperation(
                    accountId = "/virtual-accounts/user-external-eth",
                    amount = -100
                ),
                AddOperation(
                    accountId = "/crypto-custody-accounts/real-eth-coinbase",
                    amount = 100
                ),
                AddOperation(
                    accountId = "/users/$userId/profiles/$profileId/crypto-accounts/$cryptoAccountId",
                    amount = 100
                ),
            )
            val response = post(path = "/apps/trade-admin/transactions", body = operations)
            if (response.status != HttpStatusCode.OK) {
                println(response.bodyAsText())
            }
            val txn = response.body<Transaction>()
            then("transaction is saved to ledger") {
                response.status shouldBe HttpStatusCode.OK
            }
            then("account should be credited") {
                get("/apps/trade-admin/users/$userId/profiles/$profileId/accounts/$accountId")
                    .body<Account>().balance shouldBe 1_000
                get("/apps/trade-admin/users/$userId/profiles/$profileId/accounts/$accountId/operations")
                    .body<List<AccountOperation>>() shouldBe listOf(
                    AccountOperation(
                        transactionId = txn.id,
                        amount = 1_000,
                        balance = 1_000,
                        createdOn = txn.createdOn,
                    ))
            }
            then("custody account should be credited") {
                get("/apps/trade-admin/custody-accounts/real-nok-sp1")
                    .body<CustodyAccount>().balance shouldBe 1_001_000
                get("/apps/trade-admin/custody-accounts/real-nok-sp1/operations")
                    .body<List<AccountOperation>>() shouldBe listOf(
                    AccountOperation(
                        transactionId = txn.id,
                        amount = 1_000,
                        balance = 1_001_000,
                        createdOn = txn.createdOn,
                    ))
            }
            then("crypto account should be credited") {
                get("/apps/trade-admin/users/$userId/profiles/$profileId/crypto-accounts/$cryptoAccountId")
                    .body<Account>().balance shouldBe 100
                get("/apps/trade-admin/users/$userId/profiles/$profileId/crypto-accounts/$cryptoAccountId/operations")
                    .body<List<AccountOperation>>() shouldBe listOf(
                    AccountOperation(
                        transactionId = txn.id,
                        amount = 100,
                        balance = 100,
                        createdOn = txn.createdOn,
                    ))
            }
            then("crypto custody account should be credited") {
                get("/apps/trade-admin/crypto-custody-accounts/real-eth-coinbase")
                    .body<CustodyAccount>().balance shouldBe 1_000_100
                get("/apps/trade-admin/crypto-custody-accounts/real-eth-coinbase/operations")
                    .body<List<AccountOperation>>() shouldBe listOf(
                    AccountOperation(
                        transactionId = txn.id,
                        amount = 100,
                        balance = 1_000_100,
                        createdOn = txn.createdOn,
                    ))
            }
        }
    }
}

@Serializable
data class AddOperation(
    val accountId: String,
    val amount: Long
)

@Serializable
data class Transaction(
    val id: String,
    val createdOn: String,
)

@Serializable
data class AccountOperation(
    val transactionId: String,
    val amount: Long,
    val balance: Long,
    val createdOn: String,
)