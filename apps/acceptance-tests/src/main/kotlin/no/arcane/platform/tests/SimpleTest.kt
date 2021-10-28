package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import java.time.Instant
import java.util.*

class SimpleTest : StringSpec({

    "call /ping" {
        val response = apiClient.get<String> {
            url { path("/ping") }
        }
        response shouldBe "pong"
    }

    "call /utc" {
        val response = apiClient.get<String> {
            url { path("/utc") }
            headers {
                appendEndpointsApiUserInfoHeader()
            }
        }
        Instant.parse(response) shouldBeBefore Instant.now()
        Instant.parse(response) shouldBeAfter Instant.now().minusSeconds(7)
    }

    "call /whoami" {
        val response = apiClient.get<String> {
            url { path("/whoami") }
            headers {
                appendEndpointsApiUserInfoHeader()
            }
        }
        response shouldBe userInfoJson
    }
})

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
