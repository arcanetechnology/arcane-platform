package no.arcane.platform.tests

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import no.arcane.platform.tests.utils.apiClient
import no.arcane.platform.tests.utils.appendEndpointsApiUserInfoHeader
import java.util.*

class UserTest : BehaviorSpec({

    suspend fun getUser(userId: String): HttpResponse {
        return apiClient.get {
            url(path = "user")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
                expectSuccess = false
            }
        }
    }

    given("user does not exists") {
        val userId = UUID.randomUUID().toString()
        `when`("GET /user to check if unregistered user exists") {
            then("response is 404 NOT FOUND") {
                getUser(userId = userId).status shouldBe HttpStatusCode.NotFound
            }
        }
        `when`("POST /user to register a user") {
            val user = apiClient.post {
                url(path = "user")
                headers {
                    appendEndpointsApiUserInfoHeader(userId)
                }
            }.body<User>()
            then("response should be user object") {
                user.userId shouldBe userId
            }
            then("GET /user to check if registered user exists, should be user object") {
                getUser(userId = userId).body<User>().userId shouldBe userId
            }
        }
    }
})

@Serializable
data class User(
    val userId: String,
    val analyticsId: String,
)