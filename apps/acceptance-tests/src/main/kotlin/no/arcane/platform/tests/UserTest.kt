package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import java.util.*

@kotlin.time.ExperimentalTime
class UserTest : StringSpec({

    val userId = UUID.randomUUID().toString()

    "GET /user -> Check if unregistered user exists" {
        apiClient.get {
            url(path = "user")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
            expectSuccess = false
        }
    }

    "POST /user -> Register user" {
        apiClient.post {
            url(path = "user")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }.body<User>()
    }

    "GET /user -> Check if registered user exists" {
        apiClient.get {
            url(path = "user")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }.body<User>()
    }
})

@Serializable
data class User(
    val userId: String,
    val analyticsId: String,
)