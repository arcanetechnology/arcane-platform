package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.time.Instant
import java.util.*

class PingTest : StringSpec({

    "GET /ping" {
        val response: String = apiClient.get {
            url(path = "ping")
        }.bodyAsText()
        response shouldBe "pong"
    }

    "POST /ping" {
        val response: String = apiClient.post {
            url(path = "ping")
        }.bodyAsText()
        response shouldBe "pong"
    }

    "GET /utc" {
        val response: String = apiClient.get {
            url(path = "utc")
            headers {
                appendEndpointsApiUserInfoHeader(UUID.randomUUID().toString())
            }
        }.bodyAsText()
        Instant.parse(response) shouldBeBefore Instant.now()
        Instant.parse(response) shouldBeAfter Instant.now().minusSeconds(7)
    }
})
