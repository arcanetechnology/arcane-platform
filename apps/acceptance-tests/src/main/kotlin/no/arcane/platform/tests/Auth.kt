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

@OptIn(InternalAPI::class)
fun HeadersBuilder.appendEndpointsApiUserInfoHeader(subject: String) {
    if (System.getenv("BACKEND_HOST") == "test.api.arcane.no") {
        val idToken: String = runBlocking {
            oauthProviderEmulatorClient.get(path = "id-token") {
                contentType(ContentType.Application.Json)
                body = IdTokenClaims(
                    name = "Test User",
                    picture = "https://picsum.photos/200",
                    subject = subject,
                    email = "test@arcane.no",
                )
            }
        }
        append("Authorization", "Bearer $idToken")
    } else {
        append("X-Endpoint-API-UserInfo", Base64.getEncoder().encodeToString(userInfoJson(subject).toByteArray()))
    }
}

fun userInfoJson(subject: String) = """
{
    "aud": "acceptance-tests",
    "sub": "$subject",
    "email_verified": true,
    "user_id": "$subject",
    "name": "Test User",
    "iss": "oauth2-provider-emulator",
    "picture": "https://picsum.photos/200",
    "email": "test@arcane.no"
}
""".trimIndent()