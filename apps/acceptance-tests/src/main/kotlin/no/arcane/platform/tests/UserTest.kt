package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay

@kotlin.time.ExperimentalTime
class UserTest : StringSpec({

    "GET /user -> Check if unregistered user exists" {
        apiClient.get<Unit>(path = "user") {
            headers {
                appendEndpointsApiUserInfoHeader()
            }
            expectSuccess = false
        }
    }

    "POST /user -> Register user" {
        apiClient.post<Unit>(path = "user") {
            headers {
                appendEndpointsApiUserInfoHeader()
            }
        }
    }

    "GET /user -> Check if registered user exists" {
        apiClient.get<Unit>(path = "user") {
            headers {
                appendEndpointsApiUserInfoHeader()
            }
        }
    }
})
