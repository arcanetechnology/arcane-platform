package no.arcane.platform.tests

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*

val apiClient = HttpClient(CIO) {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
    install(UserAgent) {
        agent = "arcane-platform/apps/acceptance-tests"
    }
    defaultRequest {
        host = "arcane-platform-app"
        port = 8080
    }
}