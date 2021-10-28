package no.arcane.platform.tests

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.logging.*

val apiClient = HttpClient(CIO) {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.INFO
    }
}