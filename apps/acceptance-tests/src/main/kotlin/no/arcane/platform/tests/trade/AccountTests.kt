package no.arcane.platform.tests.trade

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Account(
    val id: String,
    val custodyAccountId: String,
    val balance: Long = 0,
    val reservedBalance: Long = 0,
    val currency: String,
    val alias: String,
    val createdOn: String,
    val updatedOn: String,
)


private suspend fun getAccounts(userId: String, profileId: String): List<Account> =
    get(path = "apps/trade-admin/users/${userId}/profiles/${profileId}/accounts").body()

private suspend fun getAccount(userId: String, profileId: String, accountId: String): Account =
    get(path = "apps/trade-admin/users/${userId}/profiles/${profileId}/accounts/${accountId}").body()

@Serializable
data class AddAccount(
    val currency: String,
    val alias: String,
    val custodyAccountId: String,
)

suspend fun addAccount(userId: String, profileId: String, addAccount: AddAccount): Account =
    post(
        path = "apps/trade-admin/users/${userId}/profiles/${profileId}/accounts",
        body = addAccount,
    ).body()

fun BehaviorSpec.accountTests() {
    given("user is registered to trade app and has a profile") {
        val userId = UUID.randomUUID().toString()
        registerUser(userId = userId)
        val profileId = addProfile(
            userId = userId,
            addProfile = AddProfile(
                alias = "Test Profile",
                type = ProfileType.PERSONAL,
            )
        ).id
        `when`("GET /apps/trade-admin/users/{userId}/profiles/{profileId}/accounts") {
            val accounts = getAccounts(userId = userId, profileId = profileId)
            then("accounts should be empty") {
                accounts shouldBe emptyList()
            }
        }
        `when`("POST /apps/trade-admin/users/{userId}/profiles/{profileId}/accounts") {
            val account = addAccount(
                userId = userId,
                profileId = profileId,
                addAccount = AddAccount(currency = "NOK", alias = "Test account", custodyAccountId = "real-nok-sp1"),
            )
            then("account is added") {
                account.custodyAccountId shouldBe "real-nok-sp1"
                account.alias shouldBe "Test account"
                account.currency shouldBe "NOK"
                account.balance shouldBe 0
                account.reservedBalance shouldBe 0
            }
            then("GET .../users/_/profiles/_/accounts/{accountId} => account") {
                getAccount(userId = userId, profileId = profileId, accountId = account.id) shouldBe account
            }
        }
    }
}

private suspend fun getCryptoAccounts(userId: String, profileId: String): List<Account> =
    get(path = "apps/trade-admin/users/${userId}/profiles/${profileId}/crypto-accounts").body()

private suspend fun getCryptoAccount(userId: String, profileId: String, accountId: String): Account =
    get(path = "apps/trade-admin/users/${userId}/profiles/${profileId}/crypto-accounts/${accountId}").body()

suspend fun addCryptoAccount(userId: String, profileId: String, addAccount: AddAccount): Account =
    post(
        path = "apps/trade-admin/users/${userId}/profiles/${profileId}/crypto-accounts",
        body = addAccount,
    ).body()

fun BehaviorSpec.cryptoAccountTests() {
    given("user is registered to trade app and has a profile") {
        val userId = UUID.randomUUID().toString()
        registerUser(userId = userId)
        val profileId = addProfile(
            userId = userId,
            addProfile = AddProfile(
                alias = "Test Profile",
                type = ProfileType.PERSONAL,
            )
        ).id
        `when`("GET /apps/trade-admin/users/{userId}/profiles/{profileId}/crypto-accounts") {
            val cryptoAccounts = getCryptoAccounts(userId = userId, profileId = profileId)
            then("cryptoAccounts should be empty") {
                cryptoAccounts shouldBe emptyList()
            }
        }
        `when`("POST /apps/trade-admin/users/{userId}/profiles/{profileId}/crypto-accounts") {
            val cryptoAccount = addCryptoAccount(
                userId = userId,
                profileId = profileId,
                addAccount = AddAccount(currency = "ETH", alias = "Test crypto account", custodyAccountId = "real-eth-coinbase"),
            )
            then("cryptoAccount is added") {
                cryptoAccount.custodyAccountId shouldBe "real-eth-coinbase"
                cryptoAccount.alias shouldBe "Test crypto account"
                cryptoAccount.currency shouldBe "ETH"
                cryptoAccount.balance shouldBe 0
                cryptoAccount.reservedBalance shouldBe 0
            }
            then("GET .../users/_/profiles/_/crypto-accounts/{accountId} => crypto account") {
                getCryptoAccount(userId = userId, profileId = profileId, accountId = cryptoAccount.id) shouldBe cryptoAccount
            }
        }
    }
}