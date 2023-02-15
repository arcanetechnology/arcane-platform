package com.k33.platform.payment

import com.k33.platform.identity.auth.gcp.UserInfo
import com.k33.platform.payment.stripe.StripeClient
import com.k33.platform.user.UserId
import com.k33.platform.utils.logging.logWithMDC
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.module() {

    routing {
        authenticate("esp-v2-header") {
            route("/payment/subscribed-products") {
                get {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    logWithMDC("userId" to userId.value) {
                        try {
                            val userEmail = call.principal<UserInfo>()!!.email
                            val subscribedProducts = StripeClient.getSubscribedProducts(userEmail = userEmail)
                            if (subscribedProducts.isEmpty()) {
                                call.respond(HttpStatusCode.NotFound)
                            }
                            call.respond(SubscribedProducts(subscribedProducts = subscribedProducts))
                        } catch (e: Exception) {
                            call.application.log.error("Subscribed Products", e)
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }
                }
            }
        }
    }
}

@Serializable
data class SubscribedProducts(
    val subscribedProducts: List<String>
)