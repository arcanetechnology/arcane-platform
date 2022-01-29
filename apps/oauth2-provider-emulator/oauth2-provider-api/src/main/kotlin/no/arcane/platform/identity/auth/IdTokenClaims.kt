package no.arcane.platform.identity.auth

import kotlinx.serialization.Serializable

@Serializable
data class IdTokenClaims(
    val name: String,
    val picture: String,
    val subject: String,
    val email: String,
)