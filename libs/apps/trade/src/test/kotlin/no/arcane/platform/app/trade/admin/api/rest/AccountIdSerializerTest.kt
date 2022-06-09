package no.arcane.platform.app.trade.admin.api.rest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.arcane.platform.app.trade.ledger.db.spanner.AccountId
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioCryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.VirtualAccountId
import java.util.UUID


class AccountIdSerializerTest : StringSpec({

    @Serializable
    data class Test(
        @Serializable(with = AccountIdSerializer::class)
        val virtualAccountId: AccountId,
        @Serializable(with = AccountIdSerializer::class)
        val fiatCustodyAccountId: AccountId,
        @Serializable(with = AccountIdSerializer::class)
        val cryptoCustodyAccountId: AccountId,
        @Serializable(with = AccountIdSerializer::class)
        val fiatStakeholderAccountId: AccountId,
        @Serializable(with = AccountIdSerializer::class)
        val cryptoStakeholderAccountId: AccountId,
        @Serializable(with = AccountIdSerializer::class)
        val portfolioCryptoStakeholderAccountId: AccountId,
    )

    val userId = UUID.randomUUID().toString()
    val profileId = UUID.randomUUID().toString()
    val input = Test(
        virtualAccountId = VirtualAccountId("user-external-nok"),
        fiatCustodyAccountId = VirtualAccountId("real-nok-sp1"),
        cryptoCustodyAccountId = VirtualAccountId("real-eth-coinbase"),
        fiatStakeholderAccountId = FiatStakeholderAccountId(
            userId = userId,
            profileId = profileId,
            value = UUID.randomUUID().toString(),
        ),
        cryptoStakeholderAccountId = CryptoStakeholderAccountId(
            userId = userId,
            profileId = profileId,
            value = UUID.randomUUID().toString(),
        ),
        portfolioCryptoStakeholderAccountId = PortfolioCryptoStakeholderAccountId(
            userId = userId,
            profileId = profileId,
            accountId = UUID.randomUUID().toString(),
            portfolioId = UUID.randomUUID().toString(),
            value = UUID.randomUUID().toString(),
        ),
    )

    val json = Json {
        prettyPrint = true
    }
    val jsonString = json.encodeToString(input)
    println(jsonString)
    val output: Test = json.decodeFromString(jsonString)

    "check virtual account id" {
        output.virtualAccountId shouldBe input.virtualAccountId
    }
    "check fiat custody account id" {
        output.fiatCustodyAccountId shouldBe input.fiatCustodyAccountId
    }
    "check crypto custody account id" {
        output.cryptoCustodyAccountId shouldBe input.cryptoCustodyAccountId
    }
    "check fiat stakeholder account id" {
        output.fiatStakeholderAccountId shouldBe input.fiatStakeholderAccountId
    }
    "check crypto stakeholder account id" {
        output.cryptoStakeholderAccountId shouldBe input.cryptoStakeholderAccountId
    }
    "check portfolio crypto stakeholder account id" {
        output.portfolioCryptoStakeholderAccountId shouldBe input.portfolioCryptoStakeholderAccountId
    }
})