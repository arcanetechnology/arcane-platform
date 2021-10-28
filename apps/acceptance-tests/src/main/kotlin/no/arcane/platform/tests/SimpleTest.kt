package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import java.time.Instant

class SimpleTest : StringSpec({

    "call /ping" {
        val response = apiClient.get<String>("http://arcane-platform-app:8080/ping")
        response shouldBe "pong"
    }

    "call /utc" {
        val response = apiClient.get<String>("http://arcane-platform-app:8080/utc")
        Instant.parse(response) shouldBeBefore Instant.now()
        Instant.parse(response) shouldBeAfter Instant.now().minusSeconds(7)
    }
})