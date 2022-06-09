package no.arcane.platform.app.trade.admin.api.rest

import io.ktor.http.*
import io.ktor.server.routing.*
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccount
import no.arcane.platform.app.trade.ledger.db.spanner.account.CryptoCustodyAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.account.CryptoStakeholderAccountStore

fun Route.cryptoCustodyAccounts() {

    route("crypto-custody-accounts") {
        // get all fiat custody accounts
        get {
            CryptoCustodyAccountStore
                .getAll()
                .map { it.map(CryptoCustodyAccount::toCustodyAccount) }
                .thenRespond(HttpStatusCode.NotFound)
        }

        route("{accountId}"){
            // get crypto custody account
            get {
                CryptoCustodyAccountStore
                    .get(cryptoCustodyAccountId())
                    .map(CryptoCustodyAccount::toCustodyAccount)
                    .thenRespond(HttpStatusCode.NotFound)
            }
            get("operations") {
                CryptoCustodyAccountStore
                    .getOperations(
                        cryptoCustodyAccountId = cryptoCustodyAccountId(),
                        offset = offset(),
                        limit = limit(),
                    )
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
        }
    }
}