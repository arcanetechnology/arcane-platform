package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.ktor.client.features.*
import io.ktor.client.request.*

class TncTest : StringSpec({

    "Accept Terms and Conditions" {
        apiClient.post<Unit> {
            url { path("tnc/privacy-policy") }
            headers {
                appendEndpointsApiUserInfoHeader()
            }
            // TODO remove this
            expectSuccess = false
        }
    }

    "Check if Terms and Conditions are accepted" {
        apiClient.get<Unit> {
            url { path("tnc/privacy-policy") }
            headers {
                appendEndpointsApiUserInfoHeader()
            }
            // TODO remove this
            expectSuccess = false
        }
    }

    // TODO enable the test
    "Send Terms and Conditions in email".config(enabled = false) {
        apiClient.post<Unit> {
            url { path("tnc/privacy-policy/email") }
            headers {
                appendEndpointsApiUserInfoHeader()
            }
        }
    }
})
