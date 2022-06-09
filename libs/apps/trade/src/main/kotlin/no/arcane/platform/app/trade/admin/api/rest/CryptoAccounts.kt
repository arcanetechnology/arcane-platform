package no.arcane.platform.app.trade.admin.api.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCurrency
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoStakeholderAccount
import no.arcane.platform.app.trade.ledger.db.spanner.Ledger
import no.arcane.platform.app.trade.ledger.db.spanner.account.CryptoStakeholderAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.account.FiatStakeholderAccountStore

fun Route.cryptoAccounts() {
    route("crypto-accounts") {
        // create crypto stakeholder account
        post {
            val request = call.receive<AddCryptoAccount>()
            Ledger
                .addCryptoAccount(
                    cryptoCustodyAccountId = CryptoCustodyAccountId(request.custodyAccountId),
                    profileId = profileId(),
                    cryptoCurrency = CryptoCurrency(request.currency),
                    alias = request.alias,
                )
                .map(CryptoStakeholderAccount::toAccount)
                .thenRespond(ifError = HttpStatusCode.BadRequest)
        }
        // get all crypto stakeholder account
        get {
            CryptoStakeholderAccountStore
                .getAll(
                    profileId = profileId(),
                )
                .map { it.map(CryptoStakeholderAccount::toAccount) }
                .thenRespond(ifError = HttpStatusCode.NotFound)
        }
        route("{cryptoAccountId}") {
            // get crypto stakeholder account
            get {
                CryptoStakeholderAccountStore
                    .get(
                        cryptoStakeholderAccountId = cryptoAccountId(),
                    )
                    .map(CryptoStakeholderAccount::toAccount)
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
            // update crypto stakeholder account
            put {
                Ledger
                    .updateCryptoAccount(
                        cryptoStakeholderAccountId = cryptoAccountId(),
                        alias = call.receive<Alias>().alias,
                    )
                    .map(CryptoStakeholderAccount::toAccount)
                    .thenRespond(ifError = HttpStatusCode.BadRequest)
            }
            // delete crypto stakeholder account
            delete {
                TODO("Safely delete crypto stakeholder account")
            }
            get("operations") {
                CryptoStakeholderAccountStore
                    .getOperations(
                        cryptoStakeholderAccountId = cryptoAccountId(),
                        offset = offset(),
                        limit = limit(),
                    )
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
        }
    }
}

@Serializable
data class AddCryptoAccount(
    val currency: String,
    val alias: String,
    val custodyAccountId: String,
)