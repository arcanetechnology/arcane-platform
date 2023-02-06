package com.k33.platform.user

import io.ktor.server.application.*
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import com.k33.platform.user.UserService.fetchUser
import com.k33.platform.utils.config.readResourceWithoutWhitespace
import com.k33.platform.utils.graphql.GraphqlModulesRegistry

fun Application.module() {
    GraphqlModulesRegistry.registerSchema(readResourceWithoutWhitespace("/user.graphqls"))
    GraphqlModulesRegistry.registerDataFetcher("user") { env ->
        val userId = UserId(env.graphQlContext["userId"])
        async { userId.fetchUser() }.asCompletableFuture()
    }
}