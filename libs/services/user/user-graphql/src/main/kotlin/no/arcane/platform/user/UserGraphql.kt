package no.arcane.platform.user

import io.ktor.server.application.*
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import no.arcane.platform.user.UserService.fetchUser
import no.arcane.platform.utils.config.readResourceWithoutWhitespace
import no.arcane.platform.utils.graphql.GraphqlModulesRegistry

fun Application.module() {
    GraphqlModulesRegistry.registerSchema(readResourceWithoutWhitespace("/user.graphqls"))
    GraphqlModulesRegistry.registerDataFetcher("user") { env ->
        val userId = UserId(env.graphQlContext["userId"])
        async { userId.fetchUser() }.asCompletableFuture()
    }
}