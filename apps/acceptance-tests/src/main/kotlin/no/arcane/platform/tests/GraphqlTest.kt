package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.ktor.client.request.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*
import io.ktor.http.cio.*
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

    suspend fun queryGraphqlEndpoint() = apiClient.post<GraphqlResponse>(path = "graphql") {
        headers {
            appendEndpointsApiUserInfoHeader(userId)
        }
        contentType(ContentType.Application.Json)
        body = GraphqlRequest(
            query = """{ user { userId analyticsId } termsAndConditions(tncIds: ["platform-terms-and-conditions", "platform-allow-tracking"]) { tncId version accepted spaceId entryId fieldId timestamp } }"""
        )
    }

    "POST /graphql -> No data" {
        val response = queryGraphqlEndpoint()
        response.errors shouldBe null
        response.data shouldBe """{"user":null,"termsAndConditions":[]}"""
    }

    var user: User? = null

    "POST /user -> Register user" {

        user = apiClient.post(path = "user") {
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }
    }

    "POST /graphql -> Only user" {
        val response = queryGraphqlEndpoint()

        response.errors shouldBe null
        response.data shouldBe  """{"user":{"userId":"$userId","analyticsId":"${user!!.analyticsId}"},"termsAndConditions":[]}"""
    }

    var tnc: TncResponse? = null

    "POST /tnc/privacy-policy -> Submit Terms and Conditions" {

        tnc = apiClient.post<TncResponse>(path = "tnc/platform-terms-and-conditions") {
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
            contentType(ContentType.Application.Json)
            body = TncRequest(
                version = "version",
                accepted = true,
                spaceId = "spaceId",
                entryId = "entryId",
                fieldId = "fieldId",
            )
        }

    }

    "POST /graphql -> User + 1 T&C" {
        val response = queryGraphqlEndpoint()

        response.errors shouldBe null
        response.data shouldBe  """{"user":{"userId":"$userId","analyticsId":"${user!!.analyticsId}"},"termsAndConditions":[{"tncId":"platform-terms-and-conditions","version":"version","accepted":true,"spaceId":"spaceId","entryId":"entryId","fieldId":"fieldId","timestamp":"${tnc!!.timestamp}"}]}"""
    }
})