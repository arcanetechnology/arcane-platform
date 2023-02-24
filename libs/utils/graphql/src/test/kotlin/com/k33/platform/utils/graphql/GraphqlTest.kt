package com.k33.platform.utils.graphql

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.future.await
import java.io.File

class GraphqlTest : AnnotationSpec() {

    @Test
    suspend fun test() {

        //
        // init
        //

        coroutineScope {

            GraphqlModulesRegistry.registerDataFetcher("user") { env ->
                val userId: String = env.graphQlContext["userId"]
                async {
                    User(
                        userId = userId,
                        analyticsId = "analytics-id",
                    )
                }.asCompletableFuture()
            }

            GraphqlModulesRegistry.registerSchema(File("src/test/resources/user.graphqls").readText())

            //
            // sdl
            //

            GraphqlModulesRegistry.getSdl() shouldBe """
                        type App {
                          appId: String!
                        }
                        
                        type Query {
                          apps(appIds: [String]!): [App]!
                          user: User
                        }
                        
                        type User {
                          analyticsId: String!
                          userId: String!
                        }
                        
                        """.trimIndent()

            //
            // request
            //

            val graphQL: GraphQL = GraphqlModulesRegistry.getGraphQL()

            val executionInput: ExecutionInput = ExecutionInput.newExecutionInput()
                .query("""{ user { userId analyticsId } }""")
                .graphQLContext(mapOf("userId" to "user-id"))
                .build()

            val executionResult: ExecutionResult = graphQL.executeAsync(executionInput).await()

            //
            // response
            //

            executionResult.errors shouldBe emptyList()

            val data = jacksonObjectMapper().writeValueAsString(executionResult.getData<Map<String, Any?>>())
            data shouldBe """
{"user":{"userId":"user-id","analyticsId":"analytics-id"}}
        """.trimIndent()

        }
    }
}