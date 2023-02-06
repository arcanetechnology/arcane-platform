package com.k33.platform.identity.auth.gcp

import io.ktor.server.auth.*

data class UserInfo(
    val userId: String,
    val email: String,
) : Principal