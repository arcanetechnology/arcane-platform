package no.arcane.platform.app.trade.admin

import no.arcane.platform.app.trade.ledger.Currency
import no.arcane.platform.app.trade.ledger.ProfileType
import no.arcane.platform.utils.graphql.GraphqlService
import java.math.BigInteger
import java.time.ZoneOffset
import java.time.ZonedDateTime

object GraphqlDummyDataFetcher {
    context (GraphqlService)
    fun registerAsDataFetcher() {

        registerDataFetcher("fiatCustodyAccounts") {
            listOf(
                CustodyAccount(
                    id = "",
                    balance = BigInteger.ZERO,
                    currency = Currency.NOK,
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
                    balance = BigInteger.ZERO,
                    currency = "BTC",
                    alias = "",
                    createdOn = ZonedDateTime.now(ZoneOffset.UTC),
                    updatedOn = ZonedDateTime.now(ZoneOffset.UTC),
                )
            )
        }

        registerDataFetcher("users") {
            listOf(
                User(
                    id = "",
                    email = "",
                )
            )
        }

        registerDataFetcher("user") {
            TradeUser(
                id = "",
                email = "",
                createdOn = ZonedDateTime.now(ZoneOffset.UTC),
                profiles = listOf(
                    Profile(
                        id = "",
                        alias = "",
                        type = ProfileType.PERSONAL,
                        createdOn = ZonedDateTime.now(ZoneOffset.UTC),
                        updatedOn = ZonedDateTime.now(ZoneOffset.UTC),
                        accounts = listOf(
                            StakeholderAccount(
                                id = "",
                                balance = BigInteger.ZERO,
                                currency = Currency.NOK,
                                alias = "",
                                createdOn = ZonedDateTime.now(ZoneOffset.UTC),
                                updatedOn = ZonedDateTime.now(ZoneOffset.UTC),
                                portfolios = listOf(
                                    Portfolio(
                                        id = "",
                                        alias = "",
                                        createdOn = ZonedDateTime.now(ZoneOffset.UTC),
                                        updatedOn = ZonedDateTime.now(ZoneOffset.UTC),
                                        cryptoAccounts = listOf(
                                            CryptoAccount(
                                                id = "",
                                                balance = BigInteger.ZERO,
                                                currency = "BTC",
                                                alias = "",
                                                createdOn = ZonedDateTime.now(ZoneOffset.UTC),
                                                updatedOn = ZonedDateTime.now(ZoneOffset.UTC),
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }
    }
}