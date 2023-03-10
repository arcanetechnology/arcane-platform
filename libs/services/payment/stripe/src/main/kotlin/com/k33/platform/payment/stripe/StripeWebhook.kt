package com.k33.platform.payment.stripe

import com.google.gson.JsonSyntaxException
import com.k33.platform.email.Email
import com.k33.platform.email.EmailTemplateConfig
import com.k33.platform.email.MailTemplate
import com.k33.platform.email.getEmailService
import com.k33.platform.utils.config.loadConfig
import com.k33.platform.utils.logging.NotifySlack
import com.k33.platform.utils.logging.getMarker
import com.stripe.exception.SignatureVerificationException
import com.stripe.model.Subscription
import com.stripe.net.Webhook
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun Application.module() {

    val product by lazy { System.getenv("STRIPE_PRODUCT_ID_RESEARCH_PRO") }
    val endpointSecret by lazy { System.getenv("STRIPE_WEBHOOK_ENDPOINT_SECRET") }
    val contactListId by lazy { System.getenv("SENDGRID_CONTACT_LIST_ID_K33_RESEARCH_PRO") }
    val researchProWelcomeEmail by loadConfig<EmailTemplateConfig>(
        "researchApp",
        "apps.research.researchProWelcomeEmail"
    )
    val emailService by getEmailService()

    routing {
        route("/webhooks/stripe") {
            post {
                val payload = call.receiveText()
                val sigHeader = call.request.header("Stripe-Signature")
                val event = try {
                    Webhook.constructEvent(
                        payload,
                        sigHeader,
                        endpointSecret,
                    )
                } catch (e: JsonSyntaxException) {
                    call.application.log.error("Failed to parse stripe webhook event", e)
                    throw BadRequestException("Failed to json parse Stripe webhook event json", e)
                } catch (e: SignatureVerificationException) {
                    call.application.log.error("Failed to verify signature of stripe webhook event", e)
                    throw BadRequestException("Failed to verify signature of stripe webhook event", e)
                }
                val stripeObject = event.dataObjectDeserializer.deserializeUnsafe()
                // https://stripe.com/docs/api/events/types
                if (event.type.startsWith("customer.subscription.", ignoreCase = true)) {
                    val subscription = stripeObject as Subscription
                    val customerEmail = StripeClient.getCustomerEmail(subscription.customer)
                    if (customerEmail != null) {
                        val products = subscription.items.data.map { subscriptionItem ->
                            subscriptionItem.plan.product
                        }.toSet()
                        if (products.contains(product)) {
                            when (event.type) {
                                "customer.subscription.created" -> {
                                    coroutineScope {
                                        launch {
                                            call.application.log.info(
                                                NotifySlack.NOTIFY_SLACK_RESEARCH.getMarker(),
                                                "$customerEmail has subscribed to K33 Research Pro",
                                            )
                                        }
                                        launch {
                                            emailService.sendEmail(
                                                from = Email(
                                                    address = researchProWelcomeEmail.from.email,
                                                    label = researchProWelcomeEmail.from.label,
                                                ),
                                                toList = listOf(Email(customerEmail)),
                                                mail = MailTemplate(researchProWelcomeEmail.sendgridTemplateId)
                                            )
                                        }
                                        launch {
                                            emailService.upsertMarketingContacts(
                                                contactEmails = listOf(customerEmail),
                                                contactListIds = listOf(contactListId),
                                            )
                                        }
                                    }
                                }

                                "customer.subscription.deleted" -> {
                                    coroutineScope {
                                        launch {
                                            call.application.log.info(
                                                NotifySlack.NOTIFY_SLACK_RESEARCH.getMarker(),
                                                "$customerEmail has unsubscribed to K33 Research Pro",
                                            )
                                        }
                                        launch {
                                            emailService.unlistMarketingContacts(
                                                contactEmails = listOf(customerEmail),
                                                contactListId = contactListId,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}