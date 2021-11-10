package no.arcane.platform.identity.auth

import io.ktor.auth.*

data class UserInfo(
    val userId: String,
    val email: String,
) : Principal