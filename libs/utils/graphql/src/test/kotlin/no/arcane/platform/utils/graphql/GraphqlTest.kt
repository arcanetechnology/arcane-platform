package no.arcane.platform.utils.graphql

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

            GraphqlModulesRegistry.registerDataFetcher("termsAndConditions") { env ->
                val tncIds: List<String> = env.arguments["tncIds"] as? List<String> ?: emptyList()
                async {
                    tncIds.map { tncId ->
                        Tnc(
                            tncId = tncId,
                            version = "1",
                            accepted = true,
                            spaceId = "space-id",
                            entryId = "entry-id",
                            fieldId = "field-id",
                            timestamp = "2022-01-20T14:05:00Z"
                        )
                    }
                }.asCompletableFuture()
            }

            GraphqlModulesRegistry.registerSchema(File("src/test/resources/user.graphqls").readText())
            GraphqlModulesRegistry.registerSchema(File("src/test/resources/tnc.graphqls").readText())

            //
            // sdl
            //

            GraphqlModulesRegistry.getSdl() shouldBe """
                        type App {
                          appId: String!
                        }
                        
                        type Query {
                          apps(appIds: [String]!): [App]!
                          termsAndConditions(tncIds: [String]!): [Tnc]!
                          user: User
                        }
                        
                        type Tnc {
                          accepted: Boolean!
                          entryId: String!
                          fieldId: String!
                          spaceId: String!
                          timestamp: String!
                          tncId: String!
                          version: String!
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
                .query("""{ user { userId analyticsId } termsAndConditions(tncIds: ["platform.termsAndConditions", "platform.privacyPolicy"]) { tncId version accepted spaceId entryId fieldId timestamp } }""")
                // .variables(emptyMap())
                // .variables(mapOf("tncId" to "platform.termsAndConditions"))
                // .graphQLContext(emptyMap<Any, Any>())
                .graphQLContext(mapOf("userId" to "user-id"))
                .build()

            val executionResult: ExecutionResult = graphQL.executeAsync(executionInput).await()

            //
            // response
            //

            executionResult.errors shouldBe emptyList()

            val data = jacksonObjectMapper().writeValueAsString(executionResult.getData<Map<String, Any?>>())
            data shouldBe """
{"user":{"userId":"user-id","analyticsId":"analytics-id"},"termsAndConditions":[{"tncId":"platform.termsAndConditions","version":"1","accepted":true,"spaceId":"space-id","entryId":"entry-id","fieldId":"field-id","timestamp":"2022-01-20T14:05:00Z"},{"tncId":"platform.privacyPolicy","version":"1","accepted":true,"spaceId":"space-id","entryId":"entry-id","fieldId":"field-id","timestamp":"2022-01-20T14:05:00Z"}]}
        """.trimIndent()

        }
    }
}