package no.arcane.platform.app.trade.admin.api.rest

import io.ktor.http.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCustodyAccount
import no.arcane.platform.app.trade.ledger.db.spanner.account.CryptoCustodyAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.account.FiatCustodyAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.CustodyAccount as DbCustodyAccount

fun Route.custodyAccounts() {

    route("custody-accounts") {
        // get all fiat custody accounts
        get {
            FiatCustodyAccountStore
                .getAll()
                .map { it.map(FiatCustodyAccount::toCustodyAccount) }
                .thenRespond(HttpStatusCode.NotFound)
        }

        // get fiat custody account
        route("{accountId}") {
            get {
                FiatCustodyAccountStore
                    .get(custodyAccountId())
                    .map(FiatCustodyAccount::toCustodyAccount)
                    .thenRespond(HttpStatusCode.NotFound)
            }
            get("operations") {
                FiatCustodyAccountStore
                    .getOperations(
                        fiatCustodyAccountId = custodyAccountId(),
                        offset = offset(),
                        limit = limit(),
                    )
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
        }
    }
}

@Serializable
data class CustodyAccount(
    val id: String,
    val balance: Long = 0,
    val reservedBalance: Long = 0,
    val currency: String,
    val alias: String,
    val createdOn: String,
    val updatedOn: String,
)

fun DbCustodyAccount.toCustodyAccount() = CustodyAccount(
    id = id.value,
    balance = balance,
    reservedBalance = reservedBalance,
    currency = currency.toText(),
    alias = alias,
    createdOn = createdOn.toString(),
    updatedOn = updatedOn.toString(),
)