package no.arcane.platform.app.trade.admin.api.graphql

import no.arcane.platform.app.trade.ledger.db.spanner.FiatCurrency
import no.arcane.platform.utils.graphql.GraphqlService
import java.time.ZoneOffset
import java.time.ZonedDateTime

object GraphqlDataFetchers {
    context (GraphqlService)
    fun registerAsDataFetcher() {

        registerDataFetcher("fiatCustodyAccounts") {
            listOf(
                CustodyAccount(
                    id = "",
                    balance = 0,
                    currency = FiatCurrency.NOK,
                    alias = "",
                    createdOn = ZonedDateTime.now(ZoneOffset.UTC),
                    updatedOn = ZonedDateTime.now(ZoneOffset.UTC),
                )
            )
        }

        registerDataFetcher("cryptoCustodyAccounts") {
            listOf(
                CryptoAccount(
                    id = "",
                    balance = 0,
                    currency = "BTC",
                    alias = "",
                    createdOn = ZonedDateTime.now(ZoneOffset.UTC),
                    updatedOn = ZonedDateTime.now(ZoneOffset.UTC),
                )
            )
        }

        registerDataFetcher("user") { env ->
            val userId = env.getArgument<String>("userId")
            User(
                id = userId,
                createdOn = ZonedDateTime.now(),
                profiles = emptyList()
            )
        }
    }
}