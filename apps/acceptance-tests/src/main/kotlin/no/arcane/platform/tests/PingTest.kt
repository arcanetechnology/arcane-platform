package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import java.time.Instant
import java.util.UUID

class PingTest : StringSpec({

    "GET /ping" {
        val response = apiClient.get<String>(path = "ping")
        response shouldBe "pong"
    }

    "GET /utc" {
        val response = apiClient.get<String>(path = "utc") {
            headers {
                appendEndpointsApiUserInfoHeader(UUID.randomUUID().toString())
            }
        }
        Instant.parse(response) shouldBeBefore Instant.now()
        Instant.parse(response) shouldBeAfter Instant.now().minusSeconds(7)
    }
})
