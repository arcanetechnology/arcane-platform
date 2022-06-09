package no.arcane.platform.app.trade.admin

import no.arcane.platform.app.trade.ledger.Currency
import no.arcane.platform.app.trade.ledger.ProfileType
import java.math.BigInteger
import java.time.ZonedDateTime

data class User(
    val id: String,
    val email: String,
)

data class TradeUser(
    val id: String,
    val email: String,
    val createdOn: ZonedDateTime,
    val profiles: List<Profile>,
)

data class Profile(
    val id: String,
    val alias: String,
    val type: ProfileType,
    val createdOn: ZonedDateTime,
    val updatedOn: ZonedDateTime,
    val accounts: List<StakeholderAccount>,
)

data class StakeholderAccount(
    val id: String,
    val balance: BigInteger,
    val currency: Currency,
    val alias: String,
    val createdOn: ZonedDateTime,
    val updatedOn: ZonedDateTime,
    val portfolios: List<Portfolio>,
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
    val balance: BigInteger,
    val currency: Currency,
    val alias: String,
    val createdOn: ZonedDateTime,
    val updatedOn: ZonedDateTime,
)

data class CryptoAccount(
    val id: String,
    val balance: BigInteger,
    val currency: String,
    val alias: String,
    val createdOn: ZonedDateTime,
    val updatedOn: ZonedDateTime,
)