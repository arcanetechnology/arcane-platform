package com.k33.platform.identity.auth

import com.k33.platform.identity.auth.apple.appleJwtAuthConfig
import com.k33.platform.identity.auth.gcp.gcpEndpointsAuthConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.module() {
    install(Authentication) {
        appleJwtAuthConfig()
        gcpEndpointsAuthConfig()
    }
}