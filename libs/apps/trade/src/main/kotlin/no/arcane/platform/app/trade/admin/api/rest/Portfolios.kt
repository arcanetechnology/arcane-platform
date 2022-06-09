package no.arcane.platform.app.trade.admin.api.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import no.arcane.platform.app.trade.ledger.db.spanner.Ledger
import no.arcane.platform.app.trade.ledger.db.spanner.portfolio.PortfolioStore
import no.arcane.platform.app.trade.ledger.db.spanner.Portfolio as DbPortfolio

fun Route.portfolios() {
    route("portfolios") {
        // create portfolio
        post {
            Ledger
                .addPortfolio(
                    accountId = accountId(),
                    alias = call.receive<Alias>().alias,
                )
                .map(DbPortfolio::toPortfolio)
                .thenRespond(ifError = HttpStatusCode.BadRequest)
        }
        // get all portfolios
        get {
            PortfolioStore
                .getAll(
                    fiatStakeholderAccountId = accountId(),
                )
                .map { it.map(DbPortfolio::toPortfolio) }
                .thenRespond(ifError = HttpStatusCode.NotFound)
        }
        route("{portfolioId}") {
            // get portfolio
            get {
                PortfolioStore
                    .get(
                        portfolioId = portfolioId(),
                    )
                    .map(DbPortfolio::toPortfolio)
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
            // update portfolio
            put {
                Ledger
                    .updatePortfolio(
                        portfolioId = portfolioId(),
                        alias = call.receive<Alias>().alias,
                    )
                    .map(DbPortfolio::toPortfolio)
                    .thenRespond(ifError = HttpStatusCode.BadRequest)
            }
            // delete portfolio
            delete {
                TODO("Safely delete portfolio")
            }
            cryptoAccounts()
        }
    }
}

@Serializable
data class Portfolio(
    val id: String,
    val alias: String,
    val createdOn: String,
    val updatedOn: String,
)

fun DbPortfolio.toPortfolio() = Portfolio(
    id = portfolioId.value,
    alias = alias,
    createdOn = createdOn.toString(),
    updatedOn = updatedOn.toString(),
)