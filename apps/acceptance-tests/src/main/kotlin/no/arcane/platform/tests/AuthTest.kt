package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*

class AuthTest : StringSpec({

    "Parse gcp esp v2 headers" {
        val response = apiClient.get<String> {
            url { path("whoami") }
            headers {
                appendEndpointsApiUserInfoHeader()
            }
        }
        response shouldBe userInfoJson
    }
})
