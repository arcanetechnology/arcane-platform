package com.k33.platform.identity.auth

import io.ktor.server.application.*
import io.ktor.server.auth.*
import com.k33.platform.identity.auth.apple.appleJwtAuthConfig
import com.k33.platform.identity.auth.gcp.gcpEndpointsAuthConfig

fun Application.module() {
    install(Authentication) {
        appleJwtAuthConfig()
        gcpEndpointsAuthConfig()
    }
}