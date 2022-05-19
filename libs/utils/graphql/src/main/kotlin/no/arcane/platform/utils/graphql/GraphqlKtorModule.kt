package no.arcane.platform.utils.graphql

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.ExecutionInput
import graphql.ExecutionResult
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.future.await
import kotlinx.serialization.Serializable
import no.arcane.platform.identity.auth.gcp.UserInfo
import no.arcane.platform.utils.logging.logWithMDC

fun Application.module() {

    val graphQL by lazy { GraphqlModulesRegistry.getGraphQL() }

    val sdl by lazy { GraphqlModulesRegistry.getSdl() }

    val jacksonObjectMapper by lazy { jacksonObjectMapper() }

    routing {

        get("sdl") {
            call.respondText(sdl)
        }

        authenticate("esp-v2-header") {
            suspend fun handleRequest(
                call: ApplicationCall,
                graphqlQuery: String,
                userId: String,
            ) {
                val executionInput: ExecutionInput = ExecutionInput.newExecutionInput()
                    .query(graphqlQuery)
                    .graphQLContext(mapOf("userId" to userId))
                    .build()

                val executionResult: ExecutionResult = graphQL.executeAsync(executionInput).await()
                val data = jacksonObjectMapper.writeValueAsString(executionResult.getData())
                call.respond(
                    GraphqlResponse(
                        data = data,
                        errors = if (executionResult.errors.isNullOrEmpty()) {
                            null
                        } else {
                            executionResult.errors.map { it.toString() }
                        }
                    )
                )
            }
            get ("graphql") {
                val userId = call.principal<UserInfo>()!!.userId
                logWithMDC("userId" to userId) {
                    val graphqlQuery = call.request.queryParameters["query"]
                    if (graphqlQuery == null) {
                        call.respond(HttpStatusCode.BadRequest, "query param - 'query' is mandatory")
                    } else {
                        handleRequest(call, graphqlQuery, userId)
                    }
                }
            }
            post("graphql") {
                val userId = call.principal<UserInfo>()!!.userId
                logWithMDC("userId" to userId) {
                    val graphqlQuery = call.receive<GraphqlRequest>().query
                    handleRequest(call, graphqlQuery, userId)
                }
            }
        }
    }
}

@Serializable
data class GraphqlRequest(
    val query: String
)

@Serializable
data class GraphqlResponse(
    val data: String,
    val errors: List<String>? = null,
)