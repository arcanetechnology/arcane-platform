package com.k33.platform.emailsubscription

import com.k33.platform.email.getEmailService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.module() {

    val emailService by getEmailService()

    routing {
        route("/email-subscriptions") {
            put {
                val request = call.receive<EmailSubscriptionsRequest>()
                val success = emailService.upsertMarketingContacts(
                    contactEmails = request.emails,
                    contactListIds = request.listIds,
                )
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

@Serializable
data class EmailSubscriptionsRequest(
    val emails: List<String>,
    val listIds: List<String>,
)