package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*

class GraphqlTest : StringSpec({

    @Serializable
    data class GraphqlRequest(
        val query: String
    )

    @Serializable
    data class GraphqlResponse(
        val data: String,
        val errors: List<String>? = null,
    )

    val userId = UUID.randomUUID().toString()

    suspend fun queryGraphqlEndpoint(): GraphqlResponse = apiClient.post {
        url(path = "graphql")
        headers {
            appendEndpointsApiUserInfoHeader(userId)
        }
        contentType(ContentType.Application.Json)
        setBody(
            GraphqlRequest(
                query = """{ user { userId analyticsId } termsAndConditions(tncIds: ["platform.termsAndConditions", "platform.privacyPolicy"]) { tncId version accepted spaceId environmentId entryId fieldId timestamp } }"""
            )
        )
    }.body()

    "POST /graphql -> No data" {
        val response = queryGraphqlEndpoint()
        response.errors shouldBe null
        response.data shouldBe """{"user":null,"termsAndConditions":[]}"""
    }

    var user: User? = null

    "POST /user -> Register user" {

        user = apiClient.post {
            url(path = "user")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }.body()
    }

    "POST /graphql -> Only user" {
        val response = queryGraphqlEndpoint()

        response.errors shouldBe null
        response.data shouldBe """{"user":{"userId":"$userId","analyticsId":"${user!!.analyticsId}"},"termsAndConditions":[]}"""
    }

    val tncRequest = TncRequest(
        version = System.getenv("PLATFORM_TNC_VERSION"),
        accepted = true,
        spaceId = System.getenv("LEGAL_SPACE_ID"),
        environmentId = System.getenv("PLATFORM_TNC_ENV_ID"),
        entryId = System.getenv("PLATFORM_TNC_ENTRY_ID"),
        fieldId = "contentOfLegalText",
    )
    var tnc: TncResponse? = null

    "POST /tnc/platform.termsAndConditions -> Submit Terms and Conditions" {

        tnc = apiClient.post {
            url(path = "tnc/platform.termsAndConditions")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
            contentType(ContentType.Application.Json)
            setBody(tncRequest)
        }.body()

    }

    "POST /graphql -> User + 1 T&C" {
        val response = queryGraphqlEndpoint()

        response.errors shouldBe null
        response.data shouldBe """{"user":{"userId":"$userId","analyticsId":"${user!!.analyticsId}"},"termsAndConditions":[{"tncId":"platform.termsAndConditions","version":"${tncRequest.version}","accepted":${tncRequest.accepted},"spaceId":"${tncRequest.spaceId}","environmentId":"${tncRequest.environmentId}","entryId":"${tncRequest.entryId}","fieldId":"${tncRequest.fieldId}","timestamp":"${tnc!!.timestamp}"}]}"""
    }
})