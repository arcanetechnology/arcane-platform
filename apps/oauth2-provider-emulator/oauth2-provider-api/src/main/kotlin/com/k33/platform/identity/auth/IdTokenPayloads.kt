package com.k33.platform.identity.auth

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
    val email: String = "test@k33.com",
)

@Serializable
data class AppleIdTokenPayload(
    @SerialName("iss") val issuer: String = "oauth2-provider-emulator",
    @SerialName("sub") val subject: String,
    @SerialName("aud") val audience: String = "acceptance-tests",
    val name: String = "Test User",
    val email: String = "test@k33.com",
    @SerialName("email_verified") val emailVerified: Boolean = true,
    @SerialName("is_private_email") val isPrivateEmail: Boolean = false,
    @SerialName("real_user_status") val realUserStatus: Int = 0,
)