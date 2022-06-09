package no.arcane.platform.app.trade.admin.api.graphql

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.ExecutionInput
import graphql.ExecutionResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.future.await
import no.arcane.platform.identity.auth.gcp.AdminInfo
import no.arcane.platform.utils.graphql.GraphqlRequest
import no.arcane.platform.utils.graphql.GraphqlResponse
import no.arcane.platform.utils.logging.logWithMDC

fun Route.graphqlEndpoint() {

    val sdl by lazy { TradeAdminGraphqlService.getSdl() }
    val jacksonObjectMapper = lazy { jacksonObjectMapper() }
    val graphql by lazy { TradeAdminGraphqlService.getGraphQL() }

    suspend fun handleRequest(
        call: ApplicationCall,
        graphqlQuery: String,
    ) {
        val executionInput: ExecutionInput = ExecutionInput.newExecutionInput()
            .query(graphqlQuery)
            .build()

        val executionResult: ExecutionResult = graphql.executeAsync(executionInput).await()
        val data = jacksonObjectMapper.value.writeValueAsString(executionResult.getData())
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

    route("apps/trade-admin") {
        get("sdl") {
            call.respondText(sdl)
        }

        route("graphql") {
            authenticate("trade-admin-auth") {
                get {
                    val adminInfo = call.principal<AdminInfo>()!!
                    logWithMDC("adminEmail" to adminInfo.email) {
                        val graphqlQuery = call.request.queryParameters["query"]
                        if (graphqlQuery == null) {
                            call.respond(HttpStatusCode.BadRequest, "query param - 'query' is mandatory")
                        } else {
                            handleRequest(call, graphqlQuery)
                        }
                    }
                }

                post {
                    val adminInfo = call.principal<AdminInfo>()!!
                    logWithMDC("adminEmail" to adminInfo.email) {
                        val graphqlQuery = call.receive<GraphqlRequest>().query
                        handleRequest(call, graphqlQuery)
                    }
                }
            }
        }
    }
}