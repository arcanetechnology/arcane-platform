package no.arcane.platform.tnc

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.arcane.platform.cms.getCmsService
import no.arcane.platform.email.getEmailService
import no.arcane.platform.identity.auth.UserInfo
import no.arcane.platform.utils.logging.getLogger

fun Application.module() {

    val logger by getLogger()

    val tncService = TncService()

    routing {
        authenticate("esp-v2-header") {
            route("/tnc") {
                route("/{tncId}") {
                    get {
                        val accepted = tncService.getAccepted(
                            userId = call.principal<UserInfo>()!!.userId,
                            tncId = call.parameters["tncId"]!!,
                        )
                        if (accepted) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            // TODO remove this
                            call.respond(HttpStatusCode.NotImplemented)
                        }
                    }

                    post {
                        val accepted = tncService.accepted(
                            userId = call.principal<UserInfo>()!!.userId,
                            tncId = call.parameters["tncId"]!!,
                        )
                        if (accepted) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            // TODO remove this
                            call.respond(HttpStatusCode.NotImplemented)
                            // call.respond(HttpStatusCode.InternalServerError)
                        }
                    }

                    post("/email") {
                        val emailSent = tncService.emailTnc(
                            email = call.principal<UserInfo>()!!.email,
                            tncId = call.parameters["tncId"]!!,
                        )
                        if (emailSent) {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
            }
        }
    }
}

class TncService {

    private val logger by getLogger()

    private val emailService by getEmailService()
    private val cmsService by getCmsService()

    fun accepted(
        userId: String,
        tncId: String,
    ): Boolean {
        // TODO save in DB
        return false
    }

    fun getAccepted(
        userId: String,
        tncId: String,
    ): Boolean {
        // TODO read from DB
        return false
    }

    fun emailTnc(
        email: String,
        tncId: String,
    ): Boolean {
        val html = cmsService.getHtml(entryKey = tncId)

        if (html.isNullOrBlank()) {
            logger.error("CMS has no entry for $tncId")
            return false
        }

        return emailService.sendEmail(
            from = "do-not-reply@arcane.no",
            to = email,
            subject = "Privacy Policy",
            body = html,
        )
    }
}