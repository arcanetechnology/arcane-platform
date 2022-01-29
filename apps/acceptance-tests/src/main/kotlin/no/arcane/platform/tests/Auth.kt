package no.arcane.platform.tests

import io.kotest.common.runBlocking
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import no.arcane.platform.identity.auth.IdTokenClaims
import java.util.*


private val oauthProviderEmulatorClient = HttpClient(CIO) {
    install(JsonFeature)
    defaultRequest {
        host = "oauth2-provider-emulator"
        port = 8080
    }
}

private val idToken: String by lazy {
    runBlocking {
        oauthProviderEmulatorClient.get(path = "id-token") {
            contentType(ContentType.Application.Json)
            body = IdTokenClaims(
                name = "Test User",
                picture = "https://picsum.photos/200",
                subject = "123456789",
                email = "test@arcane.no",
            )
        }
    }
}

@OptIn(InternalAPI::class)
fun HeadersBuilder.appendEndpointsApiUserInfoHeader() {
    if (System.getenv("BACKEND_HOST") == "test.api.arcane.no") {
        append("Authorization", "Bearer $idToken")
    } else {
        append("X-Endpoint-API-UserInfo", Base64.getEncoder().encodeToString(userInfoJson.toByteArray()))
    }
}

val userInfoJson = """
{
    "aud": "acceptance-tests",
    "sub": "123456789",
    "email_verified": true,
    "user_id": "123456789",
    "name": "Test User",
    "iss": "oauth2-provider-emulator",
    "picture": "https://picsum.photos/200",
    "email": "test@arcane.no"
}
""".trimIndent()