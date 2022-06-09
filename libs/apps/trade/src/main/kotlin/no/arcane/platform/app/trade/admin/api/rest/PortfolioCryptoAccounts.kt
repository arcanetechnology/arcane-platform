package no.arcane.platform.app.trade.admin.api.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCurrency
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.Ledger
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioCryptoStakeholderAccount
import no.arcane.platform.app.trade.ledger.db.spanner.account.FiatCustodyAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.account.PortfolioCryptoStakeholderAccountStore

fun Route.portfolioCryptoAccounts() {
    route("crypto-accounts") {
        // create crypto stakeholder account
        post {
            val request = call.receive<CreatePortfolioCryptoAccount>()
            Ledger
                .addPortfolioCryptoAccount(
                    cryptoCustodyAccountId = CryptoCustodyAccountId(request.cryptoCustodyAccountId),
                    portfolioId = portfolioId(),
                    cryptoCurrency = CryptoCurrency(request.currency),
                    alias = request.alias,
                )
                .map(PortfolioCryptoStakeholderAccount::toAccount)
                .thenRespond(ifError = HttpStatusCode.BadRequest)
        }
        // get all crypto stakeholder account
        get {
            PortfolioCryptoStakeholderAccountStore
                .getAll(
                    portfolioId = portfolioId(),
                )
                .map { it.map(PortfolioCryptoStakeholderAccount::toAccount) }
                .thenRespond(ifError = HttpStatusCode.NotFound)
        }
        route("{cryptoAccountId}") {
            // get crypto stakeholder account
            get {
                PortfolioCryptoStakeholderAccountStore
                    .get(
                        portfolioCryptoStakeholderAccountId = portfolioCryptoAccountId(),
                    )
                    .map(PortfolioCryptoStakeholderAccount::toAccount)
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
            // update crypto stakeholder account
            put {
                Ledger
                    .updatePortfolioCryptoAccount(
                        portfolioCryptoStakeholderAccountId = portfolioCryptoAccountId(),
                        alias = call.receive<Alias>().alias,
                    )
                    .map(PortfolioCryptoStakeholderAccount::toAccount)
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
            // delete crypto stakeholder account
            delete {
                TODO("Safely delete crypto stakeholder account")
            }
            get("operations") {
                PortfolioCryptoStakeholderAccountStore
                    .getOperations(
                        portfolioCryptoStakeholderAccountId = portfolioCryptoAccountId(),
                        offset = offset(),
                        limit = limit(),
                    )
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
        }
    }
}

@Serializable
data class CreatePortfolioCryptoAccount(
    val currency: String,
    val alias: String,
    val cryptoCustodyAccountId: String,
)