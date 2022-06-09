package no.arcane.platform.identity.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FirebaseIdTokenPayload(
    @SerialName("aud") val audience: String = "acceptance-tests",
    @SerialName("sub") val subject: String,
    @SerialName("email_verified") val emailVerified: Boolean = true,
    @SerialName("user_id") val userId: String = subject,
    val name: String = "Test User",
    @SerialName("iss") val issuer: String = "oauth2-provider-emulator",
    val picture: String = "https://picsum.photos/200",
    val email: String = "test@arcane.no",
)

@Serializable
data class AppleIdTokenPayload(
    @SerialName("iss") val issuer: String = "oauth2-provider-emulator",
    @SerialName("sub") val subject: String,
    @SerialName("aud") val audience: String = "acceptance-tests",
    val name: String = "Test User",
    val email: String = "test@arcane.no",
    @SerialName("email_verified") val emailVerified: Boolean = true,
    @SerialName("is_private_email") val isPrivateEmail: Boolean = false,
    @SerialName("real_user_status") val realUserStatus: Int = 0,
)

@Serializable
data class AdminIdTokenPayload(
    @SerialName("aud") val audience: String = "acceptance-tests",
    @SerialName("sub") val subject: String,
    @SerialName("email_verified") val emailVerified: Boolean = true,
    @SerialName("user_id") val userId: String = subject,
    @SerialName("iss") val issuer: String = "oauth2-provider-emulator",
    val email: String = "test@arcane.no",
    val firebase: FirebaseClaims = FirebaseClaims(
        identities = mapOf(
            "saml.google.arcane.no" to listOf(email),
            "email" to listOf(email),
        ),
        sign_in_provider = "saml.google.arcane.no",
        tenant = "trade-admin-acceptance-tests",
    )
)

@Serializable
data class FirebaseClaims(
    val identities: Map<String, List<String>>,
    val sign_in_provider: String, // TODO bug in @SerialName
    val tenant: String,
)