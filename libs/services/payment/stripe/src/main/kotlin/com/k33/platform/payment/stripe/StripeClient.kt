package com.k33.platform.payment.stripe

import com.k33.platform.utils.logging.NotifySlack
import com.k33.platform.utils.logging.getLogger
import com.k33.platform.utils.logging.getMarker
import com.stripe.exception.ApiConnectionException
import com.stripe.exception.AuthenticationException
import com.stripe.exception.CardException
import com.stripe.exception.IdempotencyException
import com.stripe.exception.PermissionException
import com.stripe.exception.RateLimitException
import com.stripe.exception.StripeException
import com.stripe.model.Customer
import com.stripe.model.Price
import com.stripe.net.RequestOptions
import com.stripe.param.CustomerSearchParams
import com.stripe.param.checkout.SessionListLineItemsParams
import com.stripe.param.checkout.SessionListParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.Instant
import com.stripe.model.billingportal.Session as StripeCustomerPortalSession
import com.stripe.model.checkout.Session as StripeCheckoutSession
import com.stripe.param.billingportal.SessionCreateParams as CustomerPortalSessionCreateParams
import com.stripe.param.checkout.SessionCreateParams as CheckoutSessionCreateParams

object StripeClient {

    private val logger by getLogger()

    private val requestOptions by lazy {
        RequestOptions.builder()
            .setApiKey(System.getenv("STRIPE_API_KEY"))
            .setClientId("k33-backend")
            .build()
    }

    private val corporatePlanCoupon by lazy { System.getenv("STRIPE_COUPON_CORPORATE_PLAN") }

    data class CheckoutSession(
        val url: String,
        val expiresAt: String,
        val priceId: String,
        val successUrl: String,
        val cancelUrl: String,
    )

    /**
     * [Stripe API - Create Checkout Session](https://stripe.com/docs/api/checkout/sessions/create)
     */
    suspend fun createOrFetchCheckoutSession(
        customerEmail: String,
        priceId: String,
        successUrl: String,
        cancelUrl: String,
    ): CheckoutSession {
        val productId = stripeCall {
            Price.retrieve(priceId, requestOptions)
        }?.product ?: throw NotFound("price: [$priceId] not found")
        val customerInfo = getCustomersByEmail(customerEmail = customerEmail)
        val customerExists = customerInfo.customers.isNotEmpty()
        if (customerExists) {
            val products = getSubscribedProducts(customerInfo = customerInfo)
            if (products.contains(productId)) {
                logger.warn("Already subscribed")
                throw AlreadySubscribed
            }
            val existingSessions = getCheckoutSessions(
                customerEmail = customerEmail,
                priceId = priceId,
                successUrl = successUrl,
                cancelUrl = cancelUrl,
            )
            if (existingSessions.isNotEmpty()) {
                return existingSessions.singleOrNull() ?: existingSessions.maxBy { it.expiresAt }
            }
        }
        val params = CheckoutSessionCreateParams
            .builder()
            .setMode(CheckoutSessionCreateParams.Mode.SUBSCRIPTION)
            .setLocale(CheckoutSessionCreateParams.Locale.AUTO)
            .apply {
                if (customerExists) {
                    // in case of duplicates we give priority to one created later
                    setCustomer(customerInfo.customers.first().id)
                } else {
                    setCustomerEmail(customerEmail)
                }
                if (customerEmail.endsWith("@k33.com", ignoreCase = true)) {
                    addDiscount(
                        CheckoutSessionCreateParams
                            .Discount
                            .builder()
                            .setCoupon(corporatePlanCoupon)
                            .build()
                    )
                    setPaymentMethodCollection(CheckoutSessionCreateParams.PaymentMethodCollection.IF_REQUIRED)
                } else {
                    setSubscriptionData(
                        CheckoutSessionCreateParams.SubscriptionData.builder()
                            .setTrialPeriodDays(30L)
                            .build()
                    )
                    setAllowPromotionCodes(true)
                }
            }
            .setSuccessUrl(successUrl)
            .setCancelUrl(cancelUrl)
            .addLineItem(
                CheckoutSessionCreateParams
                    .LineItem
                    .builder()
                    .setPrice(priceId)
                    .setQuantity(1)
                    .build()
            )
            .build()
        val checkoutSession = stripeCall {
            StripeCheckoutSession.create(params, requestOptions)
        } ?: throw NotFound("Resource not found. Failed to create checkout session")
        val lineItems = stripeCall {
            checkoutSession.listLineItems(SessionListLineItemsParams.builder().build(), requestOptions)
        } ?: throw ServiceUnavailable("Missing line items in checkout session")
        return CheckoutSession(
            url = checkoutSession.url,
            expiresAt = Instant.ofEpochSecond(checkoutSession.expiresAt).toString(),
            priceId = lineItems.data.single().price.id,
            successUrl = checkoutSession.successUrl,
            cancelUrl = checkoutSession.cancelUrl,
        )
    }

    data class CustomerPortalSession(
        val url: String,
        val returnUrl: String,
    )

    suspend fun createCustomerPortalSession(
        customerEmail: String,
        returnUrl: String,
    ): CustomerPortalSession {
        val customerInfo = getCustomersByEmail(customerEmail = customerEmail)
        val params = CustomerPortalSessionCreateParams
            .builder()
            // in case of duplicates we give priority to one created later
            .setCustomer(customerInfo.customers.firstOrNull()?.id ?: throw NotFound("customer not found"))
            .setReturnUrl(returnUrl)
            .build()
        val customerPortalSession = stripeCall {
            StripeCustomerPortalSession.create(params, requestOptions)
        } ?: throw NotFound("customer not found")
        return CustomerPortalSession(
            url = customerPortalSession.url,
            returnUrl = customerPortalSession.returnUrl,
        )
    }

    suspend fun getCustomerEmail(stripeCustomerId: String): String? = stripeCall {
        Customer.retrieve(stripeCustomerId, requestOptions)
    }?.email

    data class CustomerInfo(
        val customerEmail: String,
        val customers: List<Customer>,
    )

    private suspend fun getCustomersByEmail(
        customerEmail: String
    ): CustomerInfo {
        val searchParams = CustomerSearchParams
            .builder()
            .setQuery("email:'$customerEmail'")
            .addExpand("data.subscriptions")
            .build()
        val customers = stripeCall {
            Customer.search(searchParams, requestOptions)
        }
            ?.data
            // in case of duplicates we give priority to one created later
            ?.sortedByDescending { it.created }
            ?: emptyList()
        if (customers.size > 1) {
            logger.error("Found multiple Stripe customers with email: $customerEmail")
        }
        return CustomerInfo(
            customerEmail = customerEmail,
            customers = customers,
        )
    }

    suspend fun getSubscribedProducts(
        customerEmail: String,
    ): Collection<String>? {
        val customerInfo = getCustomersByEmail(customerEmail = customerEmail)
        if (customerInfo.customers.isEmpty()) {
            return null
        }
        return getSubscribedProducts(customerInfo = customerInfo)
    }

    private fun getSubscribedProducts(
        customerInfo: CustomerInfo,
    ): Collection<String> {
        val productsList = customerInfo
            .customers
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
        val productSet = productsList.toSet()
        if (productsList.size > productSet.size) {
            logger.warn("Found duplicate subscriptions for stripe customer with email: ${customerInfo.customerEmail}")
        }
        return productSet
    }

    private suspend fun getCheckoutSessions(
        customerEmail: String,
        priceId: String,
        successUrl: String,
        cancelUrl: String,
    ): Collection<CheckoutSession> = coroutineScope {
        val details = SessionListParams.CustomerDetails
            .builder()
            .setEmail(customerEmail)
            .build()
        val params = SessionListParams
            .builder()
            .setCustomerDetails(details)
            .build()
        val sessions = stripeCall {
            StripeCheckoutSession.list(params, requestOptions)
        }
            ?.data
            ?.filter { session ->
                session.status == "open"
                        && session.hasLineItemWith(priceId = priceId)
                        && session.successUrl == successUrl
                        && session.cancelUrl == cancelUrl
            }
            ?.sortedByDescending { it.expiresAt }
            ?.map { checkoutSession ->
                async {
                    val lineItems = stripeCall {
                        checkoutSession.listLineItems(SessionListLineItemsParams.builder().build(), requestOptions)
                    } ?: throw ServiceUnavailable("Missing line items in checkout session")
                    stripeCall {
                        CheckoutSession(
                            url = checkoutSession.url,
                            expiresAt = Instant.ofEpochSecond(checkoutSession.expiresAt).toString(),
                            priceId = lineItems.data.single().price.id,
                            successUrl = checkoutSession.successUrl,
                            cancelUrl = checkoutSession.cancelUrl,
                        )
                    }
                }
            }
            ?.awaitAll()
            ?.filterNotNull()
            ?: emptyList()
        if (sessions.size > 1) {
            logger.warn("Found ${sessions.size} checkout sessions")
        }
        sessions
    }

    private suspend fun StripeCheckoutSession.hasLineItemWith(
        priceId: String
    ): Boolean = stripeCall {
        listLineItems(
            SessionListLineItemsParams.builder().build(),
            requestOptions
        )
    }
        ?.data
        ?.map { lineItem -> lineItem.price.id }
        ?.contains(priceId)
        ?: false

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

    private suspend fun <R> stripeCall(
        block: suspend () -> R
    ): R? {
        return withContext(Dispatchers.IO) {
            try {
                block()
            }
            // we are not catching InvalidRequestException since we want to distinguish between 400 Bad Request and
            // 404 Not Found.

            // https://stripe.com/docs/error-handling?lang=java#payment-errors
            // 402
            // The parameters were valid but the request failed.
            catch (e: CardException) {
                logger.error(NotifySlack.NOTIFY_SLACK_ALERTS.getMarker(), "Stripe Payment Error", e)
                throw BadRequest(e.userMessage)
            }

            // https://stripe.com/docs/error-handling?lang=java#connection-errors
            // This is not an HTTP error
            catch (e: ApiConnectionException) {
                logger.error(NotifySlack.NOTIFY_SLACK_ALERTS.getMarker(), "Stripe API Connection Exception", e)
                throw ServiceUnavailable(e.userMessage)

            }

            // https://stripe.com/docs/error-handling?lang=java#rate-limit-errors
            // 429
            // Too many requests hit the API too quickly. We recommend an exponential backoff of your requests.
            catch (e: RateLimitException) {
                logger.error(NotifySlack.NOTIFY_SLACK_ALERTS.getMarker(), "Received rate limit error from Stripe", e)
                throw TooManyRequests

            }

            // 401
            // No valid API key provided.
            catch (e: AuthenticationException) {
                logger.error(NotifySlack.NOTIFY_SLACK_ALERTS.getMarker(), "Missing Stripe API key", e)
                throw InternalServerError

            }

            // https://stripe.com/docs/error-handling?lang=java#permission-errors
            // 403
            // The API key doesn't have permissions to perform the request.
            catch (e: PermissionException) {
                logger.error(NotifySlack.NOTIFY_SLACK_ALERTS.getMarker(), "API key is missing permissions", e)
                throw InternalServerError

            }

            // https://stripe.com/docs/error-handling?lang=java#idempotency-errors
            // 400 or 404
            // You used an idempotency key for something unexpected,
            // like replaying a request but passing different parameters.
            catch (e: IdempotencyException) {
                logger.error(NotifySlack.NOTIFY_SLACK_ALERTS.getMarker(), "Stripe Idempotency Exception", e)
                throw BadRequest(e.userMessage)

            } catch (e: StripeException) {
                // https://stripe.com/docs/api/errors?lang=java
                when (e.statusCode) {
                    // The request was unacceptable, often due to missing a required parameter.
                    400 -> {
                        throw BadRequest(e.userMessage)
                    }
                    // The requested resource doesn't exist.
                    404 -> null
                    // The request conflicts with another request (perhaps due to using the same idempotent key).
                    409 -> throw InternalServerError
                    // Something went wrong on Stripe's end. (These are rare.)
                    in 500..599 -> {
                        logger.error(NotifySlack.NOTIFY_SLACK_ALERTS.getMarker(), "Stripe error", e)
                        throw ServiceUnavailable(e.userMessage)
                    }

                    else -> {
                        // Something went wrong on Stripe's end. (These are rare.)
                        throw InternalServerError
                    }
                }
            }
        }
    }
}