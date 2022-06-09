package no.arcane.platform.app.trade.ledger

import no.arcane.platform.user.UserId
import java.math.BigInteger
import java.time.Instant
import java.util.UUID

//
// User
//

data class User(
    val userId: UserId,
    val createdOn: Instant,
)

//
// Profile
//

@JvmInline
value class ProfileId(val value: UUID)

enum class ProfileType {
    PERSONAL,
    BUSINESS,
}

data class Profile(
    val profileId: ProfileId,
    val alias: String,
    val type: ProfileType,
    val createdOn: Instant,
    val updatedOn: Instant,
)

//
// Fiat
//

enum class Currency {
    CHF,
    DKK,
    EUR,
    GBP,
    NOK,
    SEK,
    USD,
}

data class Amount(
    val value: BigInteger,
    val currency: Currency,
)

//
// Fiat: Custody
//

@JvmInline
value class FiatCustodyAccountId(val value: UUID)

data class FiatCustodyAccount(
    val fiatCustodyAccountId: FiatCustodyAccountId,
    val balance: BigInteger = BigInteger.ZERO,
    val reservedBalance: BigInteger = BigInteger.ZERO,
    val currency: Currency,
    val alias: String,
    val createdOn: Instant,
    val updatedOn: Instant,
)

//
// Fiat: Stakeholder
//

@JvmInline
value class FiatStakeholderAccountId(val value: UUID)

data class FiatStakeholderAccount(
    val fiatStakeholderAccountId: FiatStakeholderAccountId,
    val fiatCustodyAccountId: FiatCustodyAccountId,
    val balance: BigInteger = BigInteger.ZERO,
    val reservedBalance: BigInteger = BigInteger.ZERO,
    val currency: Currency,
    val alias: String,
    val profileId: ProfileId,
    val createdOn: Instant,
    val updatedOn: Instant,
)

//
// Portfolio
//

@JvmInline
value class PortfolioId(val value: UUID)

data class Portfolio(
    val portfolioId: PortfolioId,
    val alias: String,
    val accountId: FiatStakeholderAccountId,
    val createdOn: Instant,
    val updatedOn: Instant,
)

//
// Crypto
//

@JvmInline
value class CryptoCurrency(val value: String)

data class Crypto(
    val value: BigInteger,
    val currency: CryptoCurrency,
)

//
// Crypto: Custody
//
@JvmInline
value class CryptoCustodyAccountId(val value: UUID)

data class CryptoCustodyAccount(
    val cryptoCustodyAccountId: CryptoCustodyAccountId,
    val balance: BigInteger = BigInteger.ZERO,
    val reservedBalance: BigInteger = BigInteger.ZERO,
    val cryptoCurrency: CryptoCurrency,
    val alias: String,
    val createdOn: Instant,
    val updatedOn: Instant,
)

@JvmInline
value class CryptoStakeholderAccountId(val value: UUID)

//
// Crypto: Stakeholder
//

data class CryptoStakeholderAccount(
    val cryptoStakeholderAccountId: CryptoStakeholderAccountId,
    val portfolioId: PortfolioId,
    val balance: BigInteger = BigInteger.ZERO,
    val reservedBalance: BigInteger = BigInteger.ZERO,
    val cryptoCurrency: CryptoCurrency,
    val alias: String,
    val createdOn: Instant,
    val updatedOn: Instant,
)