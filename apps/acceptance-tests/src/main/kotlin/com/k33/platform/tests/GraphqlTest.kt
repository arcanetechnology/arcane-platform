package com.k33.platform.tests

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
                query = """{ user { userId analyticsId } }"""
            )
        )
    }.body()

    "POST /graphql -> No data" {
        val response = queryGraphqlEndpoint()
        response.errors shouldBe null
        response.data shouldBe """{"user":null}"""
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
        response.data shouldBe """{"user":{"userId":"$userId","analyticsId":"${user!!.analyticsId}"}}"""
    }
})