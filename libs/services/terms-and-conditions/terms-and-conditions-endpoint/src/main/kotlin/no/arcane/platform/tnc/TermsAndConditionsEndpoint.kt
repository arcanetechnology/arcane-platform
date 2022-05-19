package no.arcane.platform.tnc

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import no.arcane.platform.identity.auth.gcp.UserInfo
import no.arcane.platform.tnc.TncService.getTnc
import no.arcane.platform.tnc.TncService.setTnc
import no.arcane.platform.user.UserId
import no.arcane.platform.utils.logging.logWithMDC

fun Application.module() {

    routing {
        authenticate("esp-v2-header") {
            route("/tnc/{tncId}") {

                post {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    logWithMDC("userId" to userId.value) {
                        val tncId = TncId(call.parameters["tncId"]!!)
                        val tncRequest = call.receive<TncRequest>()
                        val savedTnc = userId.setTnc(
                            tncId = tncId,
                            version = tncRequest.version,
                            accepted = tncRequest.accepted,
                            spaceId = tncRequest.spaceId,
                            environmentId = tncRequest.environmentId,
                            entryId = tncRequest.entryId,
                            fieldId = tncRequest.fieldId,
                        )
                        if (savedTnc != null) {
                            call.respond(HttpStatusCode.Created, savedTnc)
                        } else {
                            call.application.log.error("Failed to store tnc")
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }
                }

                get {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    logWithMDC("userId" to userId.value) {
                        val tncId = TncId(call.parameters["tncId"]!!)
                        val tnc = userId.getTnc(
                            tncId = tncId,
                        )
                        if (tnc != null) {
                            call.respond(tnc)
                        } else {
                            call.respond(HttpStatusCode.NotFound)
                        }
                    }
                }

                post("/email") {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    logWithMDC("userId" to userId.value) {
                        val userEmail = call.principal<UserInfo>()!!.email
                        val tncId = TncId(call.parameters["tncId"]!!)
                        val emailSent = TncService.emailTnc(
                            email = userEmail,
                            tncId = tncId,
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

@Serializable
data class TncRequest(
    val version: String,
    val accepted: Boolean,
    val spaceId: String,
    val environmentId: String,
    val entryId: String,
    val fieldId: String,
)