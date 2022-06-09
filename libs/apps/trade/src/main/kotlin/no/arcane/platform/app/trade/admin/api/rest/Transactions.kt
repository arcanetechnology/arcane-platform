package no.arcane.platform.app.trade.admin.api.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.arcane.platform.app.trade.ledger.db.spanner.AccountId
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccountOperationId
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoStakeholderAccountOperationId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCustodyAccountOperationId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccountOperationId
import no.arcane.platform.app.trade.ledger.db.spanner.Ledger
import no.arcane.platform.app.trade.ledger.db.spanner.OperationId
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioCryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioCryptoStakeholderAccountOperationId
import no.arcane.platform.app.trade.ledger.db.spanner.TransactionId
import no.arcane.platform.app.trade.ledger.db.spanner.VirtualAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.VirtualAccountOperationId
import no.arcane.platform.app.trade.ledger.db.spanner.transaction.TransactionStore
import java.text.ParseException
import no.arcane.platform.app.trade.ledger.db.spanner.Operation as DbOperation

fun Route.transactions() {
    route("transactions") {
        get {
            TransactionStore
                .get(
                    offset = offset(),
                    limit = limit(),
                )
                .thenRespond(ifError = HttpStatusCode.NotFound)
        }
        post {
            val operations = call.receive<List<AddOperation>>()
            Ledger
                .addTransaction(operations)
                .fold(
                    {
                        call.respond(HttpStatusCode.BadRequest, it)
                    },
                    {
                        call.respond(it)
                    }
                )
        }
        route("{transactionId}") {
            get {
                val strTransactionId = call.parameters["transactionId"]
                if (strTransactionId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val transactionId = TransactionId(strTransactionId)
                    TransactionStore
                        .get(transactionId)
                        .thenRespond(ifError = HttpStatusCode.NotFound)
                }
            }
            get("operations") {
                val strTransactionId = call.parameters["transactionId"]
                if (strTransactionId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val transactionId = TransactionId(strTransactionId)
                    TransactionStore
                        .getOperations(transactionId)
                        .map { it.map(DbOperation::toOperation) }
                        .thenRespond(ifError = HttpStatusCode.NotFound)
                }
            }
        }

    }
}

@Serializable
data class AddOperation(
    @Serializable(with = AccountIdSerializer::class)
    val accountId: AccountId,
    val amount: Long
)

@Serializable
data class TransactionOperation(
    @Serializable(with = OperationIdSerializer::class)
    val operationId: OperationId,
    @Serializable(with = AccountIdSerializer::class)
    val accountId: AccountId,
    val amount: Long,
    val balance: Long,
    val createdOn: String,
)

@Serializable
data class Transaction(
    val id: String,
    val createdOn: String,
)

fun DbOperation.toOperation() = TransactionOperation(
    operationId = id,
    accountId = id.accountId,
    amount = amount,
    balance = balance,
    createdOn = createdOn.toString(),
)

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = AccountId::class)
object AccountIdSerializer : KSerializer<AccountId> {

    private val virtualAccountIdRegex = Regex("^/virtual-accounts/(.+)$")
    private val fiatCustodyAccountIdRegex = Regex("^/custody-accounts/(.+)$")
    private val cryptoCustodyAccountIdRegex = Regex("^/crypto-custody-accounts/(.+)$")
    private val fiatStakeholderAccountIdRegex = Regex("^/users/(.+)/profiles/(.+)/accounts/(.+)$")
    private val cryptoStakeholderAccountIdRegex = Regex("^/users/(.+)/profiles/(.+)/crypto-accounts/(.+)$")
    private val portfolioAccountIdRegex =
        Regex("^/users/(.+)/profiles/(.+)/accounts/(.+)/portfolios/(.+)/portfolio-accounts/(.+)$")

    override fun deserialize(decoder: Decoder): AccountId {
        val string = decoder.decodeString()
        return virtualAccountIdRegex.matchEntire(string)
            ?.let { matchResult ->
                val (accountId) = matchResult.destructured
                VirtualAccountId(value = accountId)
            }
            ?: fiatCustodyAccountIdRegex.matchEntire(string)?.let { matchResult ->
                val (accountId) = matchResult.destructured
                FiatCustodyAccountId(value = accountId)
            }
            ?: cryptoCustodyAccountIdRegex.matchEntire(string)?.let { matchResult ->
                val (accountId) = matchResult.destructured
                CryptoCustodyAccountId(value = accountId)
            }
            // portfolioAccountIdRegex should be matched before fiatStakeholderAccountIdRegex
            ?: portfolioAccountIdRegex.matchEntire(string)?.let { matchResult ->
                val (userId, profileId, accountId, portfolioId, portfolioAccountId) = matchResult.destructured
                PortfolioCryptoStakeholderAccountId(
                    userId = userId,
                    profileId = profileId,
                    accountId = accountId,
                    portfolioId = portfolioId,
                    value = portfolioAccountId,
                )
            }
            ?: fiatStakeholderAccountIdRegex.matchEntire(string)?.let { matchResult ->
                val (userId, profileId, accountId) = matchResult.destructured
                FiatStakeholderAccountId(
                    userId = userId,
                    profileId = profileId,
                    value = accountId,
                )
            }
            ?: cryptoStakeholderAccountIdRegex.matchEntire(string)?.let { matchResult ->
                val (userId, profileId, accountId) = matchResult.destructured
                CryptoStakeholderAccountId(
                    userId = userId,
                    profileId = profileId,
                    value = accountId,
                )
            }
            ?: throw ParseException("$string is not a valid AccountId", 0)
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "no.arcane.platform.app.trade.api.rest.AccountIdSerializer",
        PrimitiveKind.STRING,
    )

    override fun serialize(encoder: Encoder, value: AccountId) {
        encoder.encodeString(value = value.toText())
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = OperationId::class)
object OperationIdSerializer : KSerializer<OperationId> {

    private val virtualAccountOperationIdRegex = Regex("^/virtual-accounts/(.+)/operations/(.+)$")
    private val fiatCustodyAccountOperationIdRegex = Regex("^/custody-accounts/(.+)/operations/(.+)$")
    private val cryptoCustodyAccountOperationIdRegex = Regex("^/crypto-custody-accounts/(.+)/operations/(.+)$")
    private val fiatStakeholderAccountOperationIdRegex = Regex("^/users/(.+)/profiles/(.+)/accounts/(.+)/operations/(.+)$")
    private val cryptoStakeholderAccountOperationIdRegex = Regex("^/users/(.+)/profiles/(.+)/crypto-accounts/(.+)/operations/(.+)$")
    private val portfolioAccountOperationIdRegex = Regex("^/users/(.+)/profiles/(.+)/accounts/(.+)/portfolios/(.+)/portfolio-accounts/(.+)/operations/(.+)$")

    override fun deserialize(decoder: Decoder): OperationId {
        val string = decoder.decodeString()
        return virtualAccountOperationIdRegex.matchEntire(string)
            ?.let { matchResult ->
                val (accountId, transactionId) = matchResult.destructured
                VirtualAccountOperationId(
                    accountId = VirtualAccountId(accountId),
                    transactionId = TransactionId(transactionId),
                )
            }
            ?: fiatCustodyAccountOperationIdRegex.matchEntire(string)?.let { matchResult ->
                val (accountId, transactionId) = matchResult.destructured
                FiatCustodyAccountOperationId(
                    accountId = FiatCustodyAccountId(accountId),
                    transactionId = TransactionId(transactionId),
                )
            }
            ?: cryptoCustodyAccountOperationIdRegex.matchEntire(string)?.let { matchResult ->
                val (accountId, transactionId) = matchResult.destructured
                CryptoCustodyAccountOperationId(
                    accountId = CryptoCustodyAccountId(accountId),
                    transactionId = TransactionId(transactionId),
                )
            }
            // portfolioAccountIdRegex should be matched before fiatStakeholderAccountIdRegex
            ?: portfolioAccountOperationIdRegex.matchEntire(string)?.let { matchResult ->
                val (userId, profileId, accountId, portfolioId, portfolioAccountId, transactionId) = matchResult.destructured
                PortfolioCryptoStakeholderAccountOperationId(
                    accountId = PortfolioCryptoStakeholderAccountId(
                        userId = userId,
                        profileId = profileId,
                        accountId = accountId,
                        portfolioId = portfolioId,
                        value = portfolioAccountId,
                    ),
                    transactionId = TransactionId(transactionId),
                )
            }
            ?: fiatStakeholderAccountOperationIdRegex.matchEntire(string)?.let { matchResult ->
                val (userId, profileId, accountId, transactionId) = matchResult.destructured
                FiatStakeholderAccountOperationId(
                    accountId = FiatStakeholderAccountId(
                        userId = userId,
                        profileId = profileId,
                        value = accountId,
                    ),
                    transactionId = TransactionId(transactionId),
                )
            }
            ?: cryptoStakeholderAccountOperationIdRegex.matchEntire(string)?.let { matchResult ->
                val (userId, profileId, accountId, transactionId) = matchResult.destructured
                CryptoStakeholderAccountOperationId(
                    accountId = CryptoStakeholderAccountId(
                        userId = userId,
                        profileId = profileId,
                        value = accountId,
                    ),
                    transactionId = TransactionId(transactionId),
                )
            }
            ?: throw ParseException("$string is not a valid OperationId", 0)
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "no.arcane.platform.app.trade.api.rest.OperationIdSerializer",
        PrimitiveKind.STRING,
    )

    override fun serialize(encoder: Encoder, value: OperationId) {
        encoder.encodeString(value = value.asText())
    }
}