package com.k33.platform.tests

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val SETTINGS_URL = "https://dev.k33.com/research/settings"

class PaymentTest : BehaviorSpec({

    val priceId = System.getenv("STRIPE_PRICE_ID_RESEARCH_PRO")!!
    val productId = System.getenv("STRIPE_PRODUCT_ID_RESEARCH_PRO")!!

    suspend fun getSubscribedProducts(
        email: String,
    ): HttpResponse {
        return apiClient.get {
            url(path = "payment/subscribed-products")
            headers {
                appendEndpointsApiUserInfoHeader(email = email)
                expectSuccess = false
            }
        }
    }

    suspend fun createOrFetchCheckoutSession(
        email: String,
    ): HttpResponse {
        return apiClient.post {
            url(path = "payment/checkout-session")
            headers {
                appendEndpointsApiUserInfoHeader(email = email)
                expectSuccess = false
            }
            contentType(ContentType.Application.Json)
            setBody(
                CheckoutSessionRequest(priceId)
            )
        }
    }

    suspend fun createCustomerPortalSession(
        email: String,
    ): HttpResponse {
        return apiClient.post {
            url(path = "payment/customer-portal-session")
            headers {
                appendEndpointsApiUserInfoHeader(email = email)
                expectSuccess = false
            }
            contentType(ContentType.Application.Json)
            setBody(
                CustomerPortalSessionRequest()
            )
        }
    }

    given("a user does not exist in stripe") {
        val email = "invalid@k33.com"
        `when`("GET /payment/subscribed-products") {
            then("response is 404 NOT FOUND") {
                getSubscribedProducts(email = email).status shouldBe HttpStatusCode.NotFound
            }
        }
        `when`("POST /payment/checkout-session") {
            val response = createOrFetchCheckoutSession(email = email)
            then("response is checkout session url with expires_at") {
                response.status shouldBe HttpStatusCode.OK
                val checkoutSession = response.body<CheckoutSession>()
                checkoutSession.priceId shouldBe priceId
                checkoutSession.successUrl shouldBe SETTINGS_URL
                checkoutSession.cancelUrl shouldBe SETTINGS_URL
            }
            // status: disabled
            // reason: since user does not exist, API is unable to fetch existing checkout session
            xand("again POST /payment/checkout-session") {
                val secondResponse = createOrFetchCheckoutSession(email = email)
                then("response should be same") {
                    secondResponse.status shouldBe HttpStatusCode.OK
                    secondResponse.body<CheckoutSession>().url shouldBe response.body<CheckoutSession>().url
                    secondResponse.body<CheckoutSession>().priceId shouldBe response.body<CheckoutSession>().priceId
                    secondResponse.body<CheckoutSession>().successUrl shouldBe response.body<CheckoutSession>().successUrl
                    secondResponse.body<CheckoutSession>().cancelUrl shouldBe response.body<CheckoutSession>().cancelUrl
                }
            }
        }
        `when`("POST /payment/customer-portal-session") {
            then("response is 404 NOT FOUND") {
                createCustomerPortalSession(email = email).status shouldBe HttpStatusCode.NotFound
            }
        }
    }

    given("user exists in stripe") {
        and("a user is NOT subscribed in stripe") {
            val email = "test.unsubscribed@k33.com"
            `when`("GET /payment/subscribed-products") {
                then("response is 404 NOT FOUND") {
                    getSubscribedProducts(email = email).status shouldBe HttpStatusCode.NotFound
                }
            }
            `when`("POST /payment/checkout-session") {
                val response = createOrFetchCheckoutSession(email = email)
                then("response is checkout session url with expires_at") {
                    response.status shouldBe HttpStatusCode.OK
                    val checkoutSession = response.body<CheckoutSession>()
                    checkoutSession.priceId shouldBe priceId
                    checkoutSession.successUrl shouldBe SETTINGS_URL
                    checkoutSession.cancelUrl shouldBe SETTINGS_URL
                }
                and("again POST /payment/checkout-session") {
                    val secondResponse = createOrFetchCheckoutSession(email = email)
                    then("response should be same") {
                        secondResponse.status shouldBe HttpStatusCode.OK
                        secondResponse.body<CheckoutSession>() shouldBe response.body<CheckoutSession>()
                    }
                }
            }
            `when`("POST /payment/customer-portal-session") {
                val response = createCustomerPortalSession(email = email)
                then("response is customer portal session url") {
                    response.status shouldBe HttpStatusCode.OK
                    response.body<CustomerPortalSession>().returnUrl shouldBe SETTINGS_URL
                }
            }
        }
        and("a user is subscribed in stripe") {
            val email = "test.subscribed@k33.com"
            `when`("GET /payment/subscribed-products") {
                val response = getSubscribedProducts(email = email)
                then("response is subscribed product id") {
                    response.status shouldBe HttpStatusCode.OK
                    response.body<SubscribedProducts>().subscribedProducts shouldBe listOf(productId)
                }
            }
            `when`("POST /payment/checkout-session") {
                then("response is 409 CONFLICT") {
                    createOrFetchCheckoutSession(email = email).status shouldBe HttpStatusCode.Conflict
                }
            }
            `when`("POST /payment/customer-portal-session") {
                val response = createCustomerPortalSession(email = email)
                then("response is customer portal session url") {
                    response.status shouldBe HttpStatusCode.OK
                    response.body<CustomerPortalSession>().returnUrl shouldBe SETTINGS_URL
                }
            }
        }
    }

})

@Serializable
data class SubscribedProducts(
    val subscribedProducts: Collection<String>
)

@Serializable
data class CheckoutSessionRequest(
    @SerialName("price_id") val priceId: String,
    @SerialName("success_url") val successUrl: String = SETTINGS_URL,
    @SerialName("cancel_url") val cancelUrl: String = SETTINGS_URL,
)

@Serializable
data class CheckoutSession(
    val url: String,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("price_id") val priceId: String,
    @SerialName("success_url") val successUrl: String,
    @SerialName("cancel_url") val cancelUrl: String,
)

@Serializable
data class CustomerPortalSessionRequest(
    @SerialName("return_url") val returnUrl: String = SETTINGS_URL,
)

@Serializable
data class CustomerPortalSession(
    val url: String,
    @SerialName("return_url") val returnUrl: String = SETTINGS_URL,
)