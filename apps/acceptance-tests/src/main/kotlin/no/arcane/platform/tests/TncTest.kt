package no.arcane.platform.tests

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

@kotlin.time.ExperimentalTime
class TncTest : BehaviorSpec({

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

    suspend fun getTnc(userId: String): HttpResponse {
        return apiClient.get {
            url(path = "tnc/platform.termsAndConditions")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
            expectSuccess = false
        }
    }

    suspend fun saveTnc(userId: String):TncResponse {
        return apiClient.post {
            url(path = "tnc/platform.termsAndConditions")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
            contentType(ContentType.Application.Json)
            setBody(tncRequest)
        }.body()
    }

    given("User has not accepted T&C") {
        val userId = UUID.randomUUID().toString()
        `when`("GET /tnc/platform.termsAndConditions") {
            then("response should be 404") {
                getTnc(userId = userId).status shouldBe HttpStatusCode.NotFound
            }
        }
        `when`("POST /tnc/platform.termsAndConditions to Submit Terms and Conditions") {
            val savedTnc = saveTnc(userId = userId)
            then("response should be 404") {
                savedTnc shouldBe tnc.copy(timestamp = savedTnc.timestamp)
            }
            then("GET /tnc/platform.termsAndConditions to Check if Terms and Conditions are saved should be 200 OK") {
                val response = getTnc(userId = userId)
                response.status shouldBe HttpStatusCode.OK
                response.body<TncResponse>() shouldBe savedTnc
            }
        }

        // TODO enable the test
        xwhen("POST /tnc/platform.privacyPolicy/email to send email") {
            then("Send Terms and Conditions in email") {
                apiClient.post {
                    url(path = "tnc/platform.privacyPolicy/email")
                    headers {
                        appendEndpointsApiUserInfoHeader(userId)
                    }
                }
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