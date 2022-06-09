package no.arcane.platform.tests.trade

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class User(
    val id: String,
    val createdOn: String,
)

private suspend fun getUser(userId: String) = get("apps/trade-admin/users/${userId}")

suspend fun registerUser(userId: String) = post("apps/trade-admin/users/${userId}")

fun BehaviorSpec.userTests() {

    given("user is registered to platform but not trade app") {
        val userId = UUID.randomUUID().toString()
        `when`("GET /apps/trade-admin/users/{userId}") {
            val response = getUser(userId = userId)
            then("response is 404 NOT FOUND") {
                response.status shouldBe HttpStatusCode.NotFound
            }
        }
        `when`("POST /apps/trade-admin/users/{userId}") {
            val user = registerUser(userId = userId)
                .body<User>()
            then("register trade user") {
                user.id shouldBe userId
            }
            then("GET /apps/trade-admin/users/{userId} => user") {
                getUser(userId = userId).body<User>().id shouldBe userId
            }
        }
    }
}