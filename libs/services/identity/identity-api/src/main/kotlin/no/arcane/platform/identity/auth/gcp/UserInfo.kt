package no.arcane.platform.identity.auth.gcp

import io.ktor.server.auth.*

data class UserInfo(
    val userId: String,
    val email: String,
) : Principal

data class AdminInfo(
    val email: String,
    val adminApp: AdminApp,
) : Principal

enum class AdminApp {
    RESEARCH,
    TRADE,
    INVEST,
}