package no.arcane.platform.tests.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*

val apiClient = HttpClient(CIO) {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
    install(UserAgent) {
        agent = "arcane-platform/apps/acceptance-tests"
    }
    install(ContentNegotiation) {
        json()
    }
    defaultRequest {
        host = backend.host
        port = backend.port
    }
}