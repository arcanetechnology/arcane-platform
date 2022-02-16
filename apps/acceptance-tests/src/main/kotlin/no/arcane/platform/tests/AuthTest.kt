package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import java.util.UUID

class AuthTest : StringSpec({

    "Parse gcp esp v2 headers" {
        val userId = UUID.randomUUID().toString()
        val response = apiClient.get<String> {
            url { path("whoami") }
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }
        response shouldBe userInfoJson(userId)
    }
})
