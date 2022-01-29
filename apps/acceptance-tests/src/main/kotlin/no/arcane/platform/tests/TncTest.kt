package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.time.ZoneOffset
import java.time.ZonedDateTime

@kotlin.time.ExperimentalTime
class TncTest : StringSpec({

    @Serializable
    data class TncRequest(
        val spaceId: String,
        val entryId: String,
        val fieldId: String,
        val version: String,
        val accepted: Boolean,
    )

    @Serializable
    data class TncResponse(
        val tncId: String,
        val spaceId: String,
        val entryId: String,
        val fieldId: String,
        val version: String,
        val accepted: Boolean,
        val timestamp: String,
    )

    val tncRequest = TncRequest(
        spaceId = "spaceId",
        entryId = "entryId",
        fieldId = "fieldId",
        version = "version",
        accepted = true
    )

    val now = ZonedDateTime.now(ZoneOffset.UTC).toString()

    val tnc = TncResponse(
        tncId = "privacy-policy",
        spaceId = "spaceId",
        entryId = "entryId",
        fieldId = "fieldId",
        version = "version",
        accepted = true,
        timestamp = now,
    )

    "POST /tnc/privacy-policy -> Submit Terms and Conditions" {

        val savedTnc = apiClient.post<TncResponse>(path = "tnc/privacy-policy") {
            headers {
                appendEndpointsApiUserInfoHeader()
            }
            contentType(ContentType.Application.Json)
            body = tncRequest
        }
        savedTnc.copy(timestamp = now) shouldBe tnc
    }

    "GET /tnc/privacy-policy -> Check if Terms and Conditions are saved" {
        val savedTnc = apiClient.get<TncResponse>(path = "tnc/privacy-policy") {
            headers {
                appendEndpointsApiUserInfoHeader()
            }
        }
        savedTnc.copy(timestamp = now) shouldBe tnc
    }

    // TODO enable the test
    "POST /tnc/privacy-policy/email -> Send Terms and Conditions in email".config(enabled = false) {
        apiClient.post<Unit>(path = "tnc/privacy-policy/email") {
            headers {
                appendEndpointsApiUserInfoHeader()
            }
        }
    }
})
