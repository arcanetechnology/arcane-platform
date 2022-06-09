package no.arcane.platform.app.trade.admin.api.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCurrency
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccount
import no.arcane.platform.app.trade.ledger.db.spanner.StakeholderAccount
import no.arcane.platform.app.trade.ledger.db.spanner.Ledger
import no.arcane.platform.app.trade.ledger.db.spanner.account.FiatStakeholderAccountStore

fun Route.fiatAccounts() {
    route("accounts") {
        // create account
        post {
            val request = call.receive<AddAccount>()
            Ledger
                .addAccount(
                    fiatCustodyAccountId = FiatCustodyAccountId(request.custodyAccountId),
                    profileId = profileId(),
                    currency = request.currency,
                    alias = request.alias,
                )
                .map(FiatStakeholderAccount::toAccount)
                .thenRespond(ifError = HttpStatusCode.BadRequest)
        }
        // get all accounts
        get {
            FiatStakeholderAccountStore
                .getAll(
                    profileId = profileId(),
                )
                .map { it.map(FiatStakeholderAccount::toAccount) }
                .thenRespond(ifError = HttpStatusCode.NotFound)
        }
        route("{accountId}") {
            // get account
            get {
                FiatStakeholderAccountStore
                    .get(
                        fiatStakeholderAccountId = accountId()
                    )
                    .map(FiatStakeholderAccount::toAccount)
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
            // update account
            put {
                Ledger
                    .updateAccount(
                        fiatStakeholderAccountId = accountId(),
                        alias = call.receive<Alias>().alias,
                    )
                    .map(FiatStakeholderAccount::toAccount)
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
            // delete account
            delete {
                TODO("Safely delete fiat stakeholder account")
            }
            get("operations") {
                FiatStakeholderAccountStore
                    .getOperations(
                        fiatStakeholderAccountId = accountId(),
                        offset = offset(),
                        limit = limit(),
                    )
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
            portfolios()
        }
    }
}

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

@Serializable
data class AddAccount(
    val currency: FiatCurrency,
    val alias: String,
    val custodyAccountId: String,
)

fun StakeholderAccount.toAccount() = Account(
    id = id.value,
    custodyAccountId = custodyAccountId.value,
    balance = balance,
    reservedBalance = reservedBalance,
    currency = currency.toText(),
    alias = alias,
    createdOn = createdOn.toString(),
    updatedOn = updatedOn.toString(),
)