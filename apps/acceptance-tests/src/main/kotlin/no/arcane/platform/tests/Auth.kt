package no.arcane.platform.tests

import io.ktor.http.*
import io.ktor.util.*
import java.util.*

@OptIn(InternalAPI::class)
fun HeadersBuilder.appendEndpointsApiUserInfoHeader() {
    append("X-Endpoint-API-UserInfo", Base64.getEncoder().encodeToString(userInfoJson.toByteArray()))
}

val userInfoJson = """
{
    "name": "Test User",
    "picture": "https://picsum.photos/200",
    "iss": "https://securetoken.google.com/arcane-platform-dev",
    "aud": "arcane-platform-dev",
    "user_id": "123456789",
    "sub": "123456789",
    "email": "test@arcane.no",
    "email_verified": true
}
""".trimIndent()