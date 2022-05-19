package no.arcane.platform.tnc

import io.ktor.server.application.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.future.asCompletableFuture
import no.arcane.platform.tnc.TncService.getTnc
import no.arcane.platform.user.UserId
import no.arcane.platform.utils.config.readResourceWithoutWhitespace
import no.arcane.platform.utils.graphql.GraphqlModulesRegistry

fun Application.module() {

    GraphqlModulesRegistry.registerSchema(readResourceWithoutWhitespace("/tnc.graphqls"))
    GraphqlModulesRegistry.registerDataFetcher("termsAndConditions") { env ->
        val userId = UserId(env.graphQlContext["userId"])
        val tncIds: List<TncId> = (env.arguments["tncIds"] as? List<String> ?: emptyList()).map(::TncId)

        async {
            tncIds.map { tncId ->
                async { userId.getTnc(tncId) }
            }.awaitAll()
                .filterNotNull()
        }.asCompletableFuture()
    }
}