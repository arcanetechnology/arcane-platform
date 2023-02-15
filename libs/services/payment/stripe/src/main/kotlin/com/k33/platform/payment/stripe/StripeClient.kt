package com.k33.platform.payment.stripe

import com.k33.platform.utils.logging.NotifySlack
import com.k33.platform.utils.logging.getLogger
import com.k33.platform.utils.logging.getMarker
import com.stripe.exception.StripeException
import com.stripe.model.Customer
import com.stripe.net.RequestOptions
import com.stripe.param.CustomerSearchParams


object StripeClient {

    private val logger by getLogger()

    private val requestOptions by lazy {
        RequestOptions.builder()
            .setApiKey(System.getenv("STRIPE_API_KEY"))
            .setClientId("k33-backend")
            .build()
    }

    fun getSubscribedProducts(userEmail: String): List<String> {
        val customers = try {
            val searchParams = CustomerSearchParams
                .builder()
                .setQuery("email:'$userEmail'")
                .addExpand("data.subscriptions")
                .build()
            Customer
                .search(searchParams, requestOptions)
                .data
        } catch (e: StripeException) {
            logger.warn("Failed to fetch Stripe Customer", e)
            // https://stripe.com/docs/api/errors?lang=java
            when (e.statusCode) {
                // The request was unacceptable, often due to missing a required parameter.
                400 -> {
                    throw Exception(e.userMessage)
                }
                // No valid API key provided.
                401 -> {
                    logger.warn(NotifySlack.NOTIFY_SLACK_ALERTS.getMarker(), "Missing Stripe API key")
                    throw Exception("Internal Server Error")
                }
                // The parameters were valid but the request failed.
                402 -> {
                    throw Exception("Internal Server Error")
                }
                // The API key doesn't have permissions to perform the request.
                403 -> {
                    logger.warn(NotifySlack.NOTIFY_SLACK_ALERTS.getMarker(), "Stripe API key is missing permissions")
                    throw Exception("Internal Server Error")
                }
                // The requested resource doesn't exist.
                404 -> return emptyList()
                // The request conflicts with another request (perhaps due to using the same idempotent key).
                409 -> throw Exception("Internal Server Error")
                // Too many requests hit the API too quickly. We recommend an exponential backoff of your requests.
                429 -> throw Exception("Internal Server Error")
                // Something went wrong on Stripe's end. (These are rare.)
                in 500..599 -> throw Exception("Internal Server Error")
                else -> throw Exception("Internal Server Error")
            }
        }
        return customers
            .flatMap { customer ->
                customer
                    .subscriptions
                    .data
                    .filter {
                        setOf(Status.active, Status.trialing)
                            .contains(Status.valueOf(it.status))
                    }
                    .flatMap { subscription ->
                        subscription
                            .items
                            .data
                            .map { subscriptionItem ->
                                subscriptionItem.price.product
                            }
                    }
            }
    }

    @Suppress("EnumEntryName")
    enum class Status {
        active,
        past_due,
        unpaid,
        canceled,
        incomplete,
        incomplete_expired,
        trialing,
        paused,
    }
}