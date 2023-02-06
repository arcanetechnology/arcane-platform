package com.k33.platform.tnc

import io.ktor.server.application.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.future.asCompletableFuture
import com.k33.platform.tnc.TncService.getTnc
import com.k33.platform.user.UserId
import com.k33.platform.utils.config.readResourceWithoutWhitespace
import com.k33.platform.utils.graphql.GraphqlModulesRegistry

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