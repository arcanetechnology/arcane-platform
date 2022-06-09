package no.arcane.platform.app.trade.admin.api.rest

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*

fun testAdminApplication(
    route: Route.() -> Unit,
    tests: suspend ApplicationTestBuilder.(HttpClient) -> Unit
) {
    testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            routing {
                route()
            }
        }
        val client = createClient {
            install(Logging)
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }
        tests(client)
    }
}