package no.arcane.platform.app.trade.admin.api.graphql

import no.arcane.platform.app.trade.ledger.db.spanner.Currency
import no.arcane.platform.app.trade.ledger.db.spanner.ProfileType
import java.time.ZonedDateTime

data class User(
    val id: String,
    val createdOn: ZonedDateTime,
    val profiles: List<Profile>,
)

data class Profile(
    val id: String,
    val alias: String,
    val type: ProfileType,
    val createdOn: ZonedDateTime,
    val updatedOn: ZonedDateTime,
    val accounts: List<Account>,
    val cryptoAccounts: List<CryptoAccount>,
)

data class Account(
    val id: String,
    val balance: Long,
    val currency: Currency,
    val alias: String,
    val createdOn: ZonedDateTime,
    val updatedOn: ZonedDateTime,
    val portfolios: List<Portfolio>,
)

data class CryptoAccount(
    val id: String,
    val balance: Long,
    val currency: String,
    val alias: String,
    val createdOn: ZonedDateTime,
    val updatedOn: ZonedDateTime,
)

data class Portfolio(
    val id: String,
    val alias: String,
    val createdOn: ZonedDateTime,
    val updatedOn: ZonedDateTime,
    val cryptoAccounts: List<CryptoAccount>,
)

data class CustodyAccount(
    val id: String,
    val balance: Long,
    val currency: Currency,
    val alias: String,
    val createdOn: ZonedDateTime,
    val updatedOn: ZonedDateTime,
)

data class PortfolioCryptoAccount(
    val id: String,
    val balance: Long,
    val currency: String,
    val alias: String,
    val createdOn: ZonedDateTime,
    val updatedOn: ZonedDateTime,
)