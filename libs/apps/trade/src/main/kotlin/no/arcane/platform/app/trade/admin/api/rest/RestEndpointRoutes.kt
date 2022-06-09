package no.arcane.platform.app.trade.admin.api.rest

import arrow.core.Either
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioCryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioId
import no.arcane.platform.app.trade.ledger.db.spanner.ProfileId
import no.arcane.platform.user.UserId

fun Route.restEndpoint() {
    authenticate("trade-admin-auth") {
        route("apps/trade-admin") {
            custodyAccounts()
            cryptoCustodyAccounts()
            users()
            transactions()
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.custodyAccountId() = FiatCustodyAccountId(
    value = call.parameters["accountId"].validate("accountId")
)

fun PipelineContext<Unit, ApplicationCall>.cryptoCustodyAccountId() = CryptoCustodyAccountId(
    value = call.parameters["accountId"].validate("accountId")
)

fun PipelineContext<Unit, ApplicationCall>.userId() =
    UserId(call.parameters["userId"] ?: throw BadRequestException("Missing mandatory parameter: userId"))

fun PipelineContext<Unit, ApplicationCall>.profileId() =
    ProfileId(
        userId = userId().value,
        value = call.parameters["profileId"].validate("profileId")
    )

fun PipelineContext<Unit, ApplicationCall>.accountId() = profileId().let {
    FiatStakeholderAccountId(
        userId = it.userId,
        profileId = it.value,
        value = call.parameters["accountId"].validate("accountId")
    )
}

fun PipelineContext<Unit, ApplicationCall>.cryptoAccountId() = profileId().let {
    CryptoStakeholderAccountId(
        userId = it.userId,
        profileId = it.value,
        value = call.parameters["cryptoAccountId"].validate("cryptoAccountId")
    )
}

fun PipelineContext<Unit, ApplicationCall>.portfolioId() = accountId().let {
    PortfolioId(
        userId = it.userId,
        profileId = it.profileId,
        accountId = it.value,
        value = call.parameters["portfolioId"].validate("portfolioId")
    )
}

fun PipelineContext<Unit, ApplicationCall>.portfolioCryptoAccountId() = portfolioId().let {
    PortfolioCryptoStakeholderAccountId(
        userId = it.userId,
        profileId = it.profileId,
        accountId = it.accountId,
        portfolioId = it.value,
        value = call.parameters["cryptoAccountId"].validate("cryptoAccountId")
    )
}

context (PipelineContext<Unit, ApplicationCall>)
        suspend inline fun <reified R : Any> Either<String, R>.thenRespond(ifError: HttpStatusCode) {
    fold(
        { errorMessage -> call.respond(ifError, errorMessage) },
        { result -> call.respond(result) }
    )
}

context (PipelineContext<Unit, ApplicationCall>)
        suspend fun Either<String, Unit>.mapErrorTo(status: HttpStatusCode) {
    fold(
        { errorMessage -> call.respond(status, errorMessage) },
        { call.respond(HttpStatusCode.OK) }
    )
}


fun String?.validate(name: String): String {
    if (this.isNullOrBlank()) {
        throw BadRequestException("Missing mandatory parameter: $name")
    }
    return this
}

@Serializable
data class Alias(
    val alias: String
)