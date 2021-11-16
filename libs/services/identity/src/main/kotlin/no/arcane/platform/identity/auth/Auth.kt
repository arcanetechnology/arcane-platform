package no.arcane.platform.identity.auth

import io.ktor.application.*
import io.ktor.auth.*
import no.arcane.platform.identity.auth.apple.appleJwtAuthConfig
import no.arcane.platform.identity.auth.gcp.gcpEndpointsAuthConfig

fun Application.module() {
    install(Authentication) {
        appleJwtAuthConfig()
        gcpEndpointsAuthConfig()
    }
}