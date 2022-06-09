package no.arcane.platform.app.trade.admin.api.rest

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCurrency
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.ProfileType
import no.arcane.platform.app.trade.ledger.db.spanner.VirtualAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.setupSpannerEmulator
import java.util.*

class AdminRestApiTest : AnnotationSpec() {

    @BeforeAll
    fun setup() {
        setupSpannerEmulator()
    }

    @Test
    fun testAdminRestApi() {
        testAdminApplication(
            route = {
                custodyAccounts()
                cryptoCustodyAccounts()
                users()
                transactions()
            }
        ) { client ->
            val userId = UUID.randomUUID().toString()
            testUserApi(
                client = client,
                userId = userId,
            )
            val profileId = testProfileApi(
                client = client,
                userId = userId,
            )
            val accountId = testAccountApi(
                client = client,
                userId = userId,
                profileId = profileId,
            )
            val cryptoAccountId = testCryptoAccountApi(
                client = client,
                userId = userId,
                profileId = profileId,
            )
            testTransactionApi(
                client = client,
                userId = userId,
                profileId = profileId,
                accountId = accountId,
                cryptoAccountId = cryptoAccountId,
            )
        }
    }

    private suspend fun testUserApi(
        client: HttpClient,
        userId: String,
    ) {
        // verify user does not exists
        suspend fun getUser() = client.get("/users/$userId")
        val response = getUser()
        response.status shouldBe HttpStatusCode.NotFound

        // register user
        suspend fun registerUser() = client.post("users/$userId").body<User>()
        val user = registerUser()
        user.id shouldBe userId

        // get registered user
        getUser().body<User>().id shouldBe userId
    }

    private suspend fun testProfileApi(
        client: HttpClient,
        userId: String,
    ): String {
        // verify profiles do not exists
        suspend fun getProfiles(): List<Profile> = client.get("/users/$userId/profiles").body()
        getProfiles() shouldBe emptyList()

        // add profile
        val alias = "Profile alias"
        suspend fun addProfile(): Profile = client.post("users/$userId/profiles") {
            contentType(ContentType.Application.Json)
            setBody(AddProfile(alias = alias, type = ProfileType.PERSONAL))
        }.body()
        var profile: Profile = addProfile()
        val createdOn = profile.createdOn
        var updatedOn = profile.updatedOn

        profile.alias shouldBe alias
        profile.type shouldBe ProfileType.PERSONAL

        // get profile
        val profileId = profile.id
        suspend fun getProfile(): Profile = client.get("/users/$userId/profiles/$profileId").body()
        profile = getProfile()
        profile.id shouldBe profileId
        profile.alias shouldBe alias
        profile.type shouldBe ProfileType.PERSONAL
        profile.createdOn shouldBe createdOn
        profile.updatedOn shouldBe updatedOn

        // update profile
        val updatedAlias = "Updated profile alias"
        suspend fun updateProfile(): Profile = client.put("/users/$userId/profiles/$profileId") {
            contentType(ContentType.Application.Json)
            setBody(Alias(alias = updatedAlias))
        }.body()
        val updatedProfile = updateProfile()
        updatedProfile.id shouldBe profileId
        updatedProfile.alias shouldBe updatedAlias
        updatedProfile.type shouldBe ProfileType.PERSONAL
        updatedProfile.createdOn shouldBe createdOn
        updatedProfile.updatedOn shouldNotBe updatedOn

        updatedOn = updatedProfile.updatedOn

        // get updated profile
        profile = getProfile()
        profile.id shouldBe profileId
        profile.alias shouldBe updatedAlias
        profile.type shouldBe ProfileType.PERSONAL
        profile.createdOn shouldBe createdOn
        profile.updatedOn shouldBe updatedOn

        return profileId
    }

    private suspend fun testAccountApi(
        client: HttpClient,
        userId: String,
        profileId: String
    ): String {

        // verify accounts do not exists
        suspend fun getAccounts(): List<Account> = client.get("/users/$userId/profiles/$profileId/accounts").body()
        getAccounts() shouldBe emptyList()

        // add account
        val alias = "Account alias"
        suspend fun addAccount(): Account = client.post("users/$userId/profiles/$profileId/accounts") {
            contentType(ContentType.Application.Json)
            setBody(AddAccount(alias = alias, currency = FiatCurrency.NOK, custodyAccountId = "real-nok-sp1"))
        }.body()
        var account: Account = addAccount()
        val createdOn = account.createdOn
        var updatedOn = account.updatedOn

        account.alias shouldBe alias
        account.currency shouldBe "NOK"
        account.custodyAccountId shouldBe "real-nok-sp1"

        // get profile
        val accountId = account.id
        suspend fun getAccount(): Account = client.get("/users/$userId/profiles/$profileId/accounts/$accountId").body()
        account = getAccount()
        account.id shouldBe accountId
        account.custodyAccountId shouldBe "real-nok-sp1"
        account.currency shouldBe "NOK"
        account.alias shouldBe alias
        account.createdOn shouldBe createdOn
        account.updatedOn shouldBe updatedOn

        // update account
        val updatedAlias = "Updated profile account"
        suspend fun updateAccount(): Account = client.put("/users/$userId/profiles/$profileId/accounts/$accountId") {
            contentType(ContentType.Application.Json)
            setBody(Alias(alias = updatedAlias))
        }.body()
        val updatedAccount = updateAccount()
        updatedAccount.id shouldBe accountId
        updatedAccount.alias shouldBe updatedAlias
        updatedAccount.custodyAccountId shouldBe "real-nok-sp1"
        updatedAccount.currency shouldBe "NOK"
        updatedAccount.createdOn shouldBe createdOn
        updatedAccount.updatedOn shouldNotBe updatedOn

        updatedOn = updatedAccount.updatedOn

        // get updated profile
        account = getAccount()
        account.id shouldBe accountId
        account.alias shouldBe updatedAlias
        account.custodyAccountId shouldBe "real-nok-sp1"
        account.currency shouldBe "NOK"
        account.createdOn shouldBe createdOn
        account.updatedOn shouldBe updatedOn

        return accountId
    }

    private suspend fun testCryptoAccountApi(
        client: HttpClient,
        userId: String,
        profileId: String
    ): String {

        // verify accounts do not exists
        suspend fun getAccounts(): List<Account> = client.get("/users/$userId/profiles/$profileId/crypto-accounts").body()
        getAccounts() shouldBe emptyList()

        // add account
        val alias = "Account alias"
        suspend fun addAccount(): Account = client.post("users/$userId/profiles/$profileId/crypto-accounts") {
            contentType(ContentType.Application.Json)
            setBody(AddCryptoAccount(alias = alias, currency = "ETH", custodyAccountId = "real-eth-coinbase"))
        }.body()
        var account: Account = addAccount()
        val createdOn = account.createdOn
        var updatedOn = account.updatedOn

        account.alias shouldBe alias
        account.currency shouldBe "ETH"
        account.custodyAccountId shouldBe "real-eth-coinbase"

        // get profile
        val accountId = account.id
        suspend fun getAccount(): Account = client.get("/users/$userId/profiles/$profileId/crypto-accounts/$accountId").body()
        account = getAccount()
        account.id shouldBe accountId
        account.custodyAccountId shouldBe "real-eth-coinbase"
        account.currency shouldBe "ETH"
        account.alias shouldBe alias
        account.createdOn shouldBe createdOn
        account.updatedOn shouldBe updatedOn

        // update account
        val updatedAlias = "Updated profile account"
        suspend fun updateAccount(): Account = client.put("/users/$userId/profiles/$profileId/crypto-accounts/$accountId") {
            contentType(ContentType.Application.Json)
            setBody(Alias(alias = updatedAlias))
        }.body()
        val updatedAccount = updateAccount()
        updatedAccount.id shouldBe accountId
        updatedAccount.alias shouldBe updatedAlias
        updatedAccount.custodyAccountId shouldBe "real-eth-coinbase"
        updatedAccount.currency shouldBe "ETH"
        updatedAccount.createdOn shouldBe createdOn
        updatedAccount.updatedOn shouldNotBe updatedOn

        updatedOn = updatedAccount.updatedOn

        // get updated profile
        account = getAccount()
        account.id shouldBe accountId
        account.alias shouldBe updatedAlias
        account.custodyAccountId shouldBe "real-eth-coinbase"
        account.currency shouldBe "ETH"
        account.createdOn shouldBe createdOn
        account.updatedOn shouldBe updatedOn

        return accountId
    }

    private suspend fun testTransactionApi(
        client: HttpClient,
        userId: String,
        profileId: String,
        accountId: String,
        cryptoAccountId: String,
    ) {
        val operations = listOf(
            AddOperation(
                accountId = VirtualAccountId("user-external-nok"),
                amount = -1000
            ),
            AddOperation(
                accountId = FiatStakeholderAccountId(
                    userId = userId,
                    profileId = profileId,
                    value = accountId,
                ),
                amount = 1000
            ),
            AddOperation(
                accountId = FiatCustodyAccountId("real-nok-sp1"),
                amount = 1000
            ),
            AddOperation(
                accountId = VirtualAccountId("user-external-eth"),
                amount = -100
            ),
            AddOperation(
                accountId = CryptoStakeholderAccountId(
                    userId = userId,
                    profileId = profileId,
                    value = cryptoAccountId,
                ),
                amount = 100
            ),
            AddOperation(
                accountId = CryptoCustodyAccountId("real-eth-coinbase"),
                amount = 100
            ),
        )
        val txn = client.post("/transactions") {
            contentType(ContentType.Application.Json)
            setBody(operations)
        }.body<Transaction>()

        client.get("/users/$userId/profiles/$profileId/accounts/$accountId")
            .body<Account>().balance shouldBe 1_000
        client.get("/users/$userId/profiles/$profileId/accounts/$accountId/operations")
            .body<List<AccountOperation>>() shouldBe listOf(
            AccountOperation(
                transactionId = txn.id,
                amount = 1_000,
                balance = 1_000,
                createdOn = txn.createdOn,
            ))

        client.get("/custody-accounts/real-nok-sp1")
            .body<CustodyAccount>().balance shouldBe 1_001_000
        client.get("/custody-accounts/real-nok-sp1/operations")
            .body<List<AccountOperation>>() shouldBe listOf(
            AccountOperation(
                transactionId = txn.id,
                amount = 1_000,
                balance = 1_001_000,
                createdOn = txn.createdOn,
            ))

        client.get("/users/$userId/profiles/$profileId/crypto-accounts/$cryptoAccountId")
            .body<Account>().balance shouldBe 100
        client.get("/users/$userId/profiles/$profileId/crypto-accounts/$cryptoAccountId/operations")
            .body<List<AccountOperation>>() shouldBe listOf(
            AccountOperation(
                transactionId = txn.id,
                amount = 100,
                balance = 100,
                createdOn = txn.createdOn,
            ))

        client.get("/crypto-custody-accounts/real-eth-coinbase")
            .body<CustodyAccount>().balance shouldBe 1_000_100
        client.get("/crypto-custody-accounts/real-eth-coinbase/operations")
            .body<List<AccountOperation>>() shouldBe listOf(
            AccountOperation(
                transactionId = txn.id,
                amount = 100,
                balance = 1_000_100,
                createdOn = txn.createdOn,
            ))

    }
}