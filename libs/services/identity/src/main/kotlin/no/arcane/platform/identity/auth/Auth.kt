package no.arcane.platform.identity.auth

import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.arcane.platform.identity.auth.admin.adminAuthConfig
import no.arcane.platform.identity.auth.apple.appleJwtAuthConfig
import no.arcane.platform.identity.auth.gcp.AdminApp
import no.arcane.platform.identity.auth.gcp.gcpEndpointsAuthConfig

fun Application.module() {
    install(Authentication) {
        appleJwtAuthConfig()
        gcpEndpointsAuthConfig()
        adminAuthConfig(
            "admin-auth",
            AdminApp.values().toList()
        )
        adminAuthConfig("trade-admin-auth", listOf(AdminApp.TRADE))
    }
}