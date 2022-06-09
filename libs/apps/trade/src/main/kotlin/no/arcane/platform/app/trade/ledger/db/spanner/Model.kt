package no.arcane.platform.app.trade.ledger.db.spanner

import no.arcane.platform.user.UserId
import java.time.Instant

//
// Currency
//

sealed interface Currency {
    fun toText(): String
}

enum class FiatCurrency : Currency {
    CHF,
    DKK,
    EUR,
    GBP,
    NOK,
    SEK,
    USD;

    override fun toText(): String = name
}

@JvmInline
value class CryptoCurrency(val value: String) : Currency {
    override fun toText(): String = value
}

//
// Account ID
//

sealed interface AccountId {
    val value: String
    fun toText(): String
}

sealed interface StakeholderAccountId : AccountId

sealed interface CustodyAccountId : AccountId


//
// Account
//

sealed interface Account {
    val id: AccountId
    val balance: Long
    val reservedBalance: Long
    val currency: Currency
    val alias: String
    val createdOn: Instant
    val updatedOn: Instant
}

sealed interface StakeholderAccount : Account {
    override val id: StakeholderAccountId
    val custodyAccountId: CustodyAccountId
}

sealed interface CustodyAccount : Account {
    override val id: CustodyAccountId
}

//
// Transaction ID
//

@JvmInline
value class TransactionId(val value: String)

//
// Operation
//

sealed interface OperationId {
    val accountId: AccountId
    val transactionId: TransactionId
    fun asText(): String = "${accountId.toText()}/operations/${transactionId.value}"
}

interface Operation {
    val id: OperationId
    val amount: Long
    val balance: Long
    val createdOn: Instant
}

//
// Virtual Account
//
data class VirtualAccountId(override val value: String) : AccountId {
    override fun toText(): String = "/virtual-accounts/$value"
}

data class VirtualAccount(
    override val id: VirtualAccountId,
    override val currency: Currency,
    override val alias: String,
) : Account {
    override val balance: Long
        get() = 0
    override val reservedBalance: Long
        get() = 0
    override val createdOn: Instant
        get() = Instant.now()
    override val updatedOn: Instant
        get() = Instant.now()
}

data class VirtualAccountOperationId(
    override val transactionId: TransactionId,
    override val accountId: VirtualAccountId,
) : OperationId

data class VirtualAccountOperation(
    override val id: VirtualAccountOperationId,
    override val amount: Long,
    override val createdOn: Instant,
) : Operation {
    override val balance: Long
        get() = 0L
}

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

data class ProfileId(
    val userId: String,
    val value: String,
)

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

//
// Fiat: Custody
//

data class FiatCustodyAccountId(override val value: String) : CustodyAccountId {
    override fun toText(): String = "/custody-accounts/$value"
}

data class FiatCustodyAccount(
    override val id: FiatCustodyAccountId,
    override val balance: Long = 0,
    override val reservedBalance: Long = 0,
    override val currency: FiatCurrency,
    override val alias: String,
    override val createdOn: Instant,
    override val updatedOn: Instant,
) : CustodyAccount

data class FiatCustodyAccountOperationId(
    override val accountId: FiatCustodyAccountId,
    override val transactionId: TransactionId,
) : OperationId

data class FiatCustodyAccountOperation(
    override val id: FiatCustodyAccountOperationId,
    override val amount: Long,
    override val balance: Long,
    override val createdOn: Instant,
) : Operation

//
// Fiat: Stakeholder
//

data class FiatStakeholderAccountId(
    val userId: String,
    val profileId: String,
    override val value: String,
) : StakeholderAccountId {
    override fun toText(): String = "/users/$userId/profiles/$profileId/accounts/$value"
}

data class FiatStakeholderAccount(
    override val id: FiatStakeholderAccountId,
    override val custodyAccountId: FiatCustodyAccountId,
    override val balance: Long = 0,
    override val reservedBalance: Long = 0,
    override val currency: FiatCurrency,
    override val alias: String,
    override val createdOn: Instant,
    override val updatedOn: Instant,
) : StakeholderAccount

data class FiatStakeholderAccountOperationId(
    override val accountId: FiatStakeholderAccountId,
    override val transactionId: TransactionId,
) : OperationId

data class FiatStakeholderAccountOperation(
    override val id: FiatStakeholderAccountOperationId,
    override val amount: Long,
    override val balance: Long,
    override val createdOn: Instant,
) : Operation

//
// Crypto
//

//
// Crypto: Custody
//

data class CryptoCustodyAccountId(override val value: String) : CustodyAccountId {
    override fun toText(): String = "/crypto-custody-accounts/$value"
}

data class CryptoCustodyAccount(
    override val id: CryptoCustodyAccountId,
    override val balance: Long = 0,
    override val reservedBalance: Long = 0,
    override val currency: CryptoCurrency,
    override val alias: String,
    override val createdOn: Instant,
    override val updatedOn: Instant,
) : CustodyAccount

data class CryptoCustodyAccountOperationId(
    override val accountId: CryptoCustodyAccountId,
    override val transactionId: TransactionId,
) : OperationId

data class CryptoCustodyAccountOperation(
    override val id: CryptoCustodyAccountOperationId,
    override val amount: Long,
    override val balance: Long,
    override val createdOn: Instant,
) : Operation

//
// Crypto: Stakeholder
//

data class CryptoStakeholderAccountId(
    val userId: String,
    val profileId: String,
    override val value: String,
) : StakeholderAccountId {
    override fun toText(): String = "/users/$userId/profiles/$profileId/crypto-accounts/$value"
}

data class CryptoStakeholderAccount(
    override val id: CryptoStakeholderAccountId,
    override val custodyAccountId: CryptoCustodyAccountId,
    override val balance: Long = 0,
    override val reservedBalance: Long = 0,
    override val currency: CryptoCurrency,
    override val alias: String,
    override val createdOn: Instant,
    override val updatedOn: Instant,
) : StakeholderAccount

data class CryptoStakeholderAccountOperationId(
    override val accountId: CryptoStakeholderAccountId,
    override val transactionId: TransactionId,
) : OperationId

data class CryptoStakeholderAccountOperation(
    override val id: CryptoStakeholderAccountOperationId,
    override val amount: Long,
    override val balance: Long,
    override val createdOn: Instant,
) : Operation

//
// Portfolio
//

data class PortfolioId(
    val userId: String,
    val profileId: String,
    val accountId: String,
    val value: String,
)

data class Portfolio(
    val portfolioId: PortfolioId,
    val alias: String,
    val createdOn: Instant,
    val updatedOn: Instant,
)

//
// Portfolio Account
//

data class PortfolioCryptoStakeholderAccountId(
    val userId: String,
    val profileId: String,
    val accountId: String,
    val portfolioId: String,
    override val value: String,
) : StakeholderAccountId {
    override fun toText(): String = "/users/$userId/profiles/$profileId/accounts/$accountId/portfolios/$portfolioId/portfolio-accounts/$value"
}

data class PortfolioCryptoStakeholderAccount(
    override val id: PortfolioCryptoStakeholderAccountId,
    override val custodyAccountId: CryptoCustodyAccountId,
    override val balance: Long = 0,
    override val reservedBalance: Long = 0,
    override val currency: CryptoCurrency,
    override val alias: String,
    override val createdOn: Instant,
    override val updatedOn: Instant,
) : StakeholderAccount

data class PortfolioCryptoStakeholderAccountOperationId(
    override val accountId: PortfolioCryptoStakeholderAccountId,
    override val transactionId: TransactionId
) : OperationId

data class PortfolioCryptoStakeholderAccountOperation(
    override val id: PortfolioCryptoStakeholderAccountOperationId,
    override val amount: Long,
    override val balance: Long,
    override val createdOn: Instant,
) : Operation