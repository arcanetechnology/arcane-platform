package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

@kotlin.time.ExperimentalTime
class TncTest : StringSpec({

    val tncRequest = TncRequest(
        version = System.getenv("PLATFORM_TNC_VERSION"),
        accepted = true,
        spaceId = System.getenv("LEGAL_SPACE_ID"),
        environmentId = System.getenv("PLATFORM_TNC_ENV_ID"),
        entryId = System.getenv("PLATFORM_TNC_ENTRY_ID"),
        fieldId = "contentOfLegalText",
    )

    val now = ZonedDateTime.now(ZoneOffset.UTC).toString()

    val tnc = TncResponse(
        tncId = "platform.termsAndConditions",
        version = tncRequest.version,
        accepted = tncRequest.accepted,
        spaceId = tncRequest.spaceId,
        environmentId = tncRequest.environmentId,
        entryId = tncRequest.entryId,
        fieldId = tncRequest.fieldId,
        timestamp = now,
    )

    val userId = UUID.randomUUID().toString()

    "POST /tnc/platform.termsAndConditions -> Submit Terms and Conditions" {

        val savedTnc: TncResponse = apiClient.post {
            url(path = "tnc/platform.termsAndConditions")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
            contentType(ContentType.Application.Json)
            setBody(tncRequest)
        }.body()
        savedTnc.copy(timestamp = now) shouldBe tnc
    }

    "GET /tnc/platform.termsAndConditions -> Check if Terms and Conditions are saved" {
        val savedTnc: TncResponse = apiClient.get {
            url(path = "tnc/platform.termsAndConditions")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }.body()
        savedTnc.copy(timestamp = now) shouldBe tnc
    }

    // TODO enable the test
    "POST /tnc/platform.privacyPolicy/email -> Send Terms and Conditions in email".config(enabled = false) {
        apiClient.post {
            url(path = "tnc/platform.privacyPolicy/email")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }
    }
})

@Serializable
data class TncRequest(
    val version: String,
    val accepted: Boolean,
    val spaceId: String,
    val environmentId: String,
    val entryId: String,
    val fieldId: String,
)

@Serializable
data class TncResponse(
    val tncId: String,
    val version: String,
    val accepted: Boolean,
    val spaceId: String,
    val environmentId: String,
    val entryId: String,
    val fieldId: String,
    val timestamp: String,
)