package no.arcane.platform.app.invest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.arcane.platform.app.invest.InvestService.isRegistered
import no.arcane.platform.app.invest.InvestService.register
import no.arcane.platform.identity.auth.gcp.UserInfo
import no.arcane.platform.user.UserId
import no.arcane.platform.utils.logging.logWithMDC

fun Application.module() {
    routing {
        authenticate("esp-v2-header") {
            route("/apps/invest/register") {
                post {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    logWithMDC("userId" to userId.value) {
                        val userEmail = call.principal<UserInfo>()!!.email
                        val fundInfoRequest = call.receive<FundInfoRequest>()
                        val validationErrors = fundInfoRequest.validate()
                        if (validationErrors.isEmpty()) {
                            if (userId.register(fundInfoRequest, userEmail)) {
                                call.respond(HttpStatusCode.OK)
                            } else {
                                call.respond(HttpStatusCode.Forbidden)
                            }
                        } else {
                            call.respond(HttpStatusCode.BadRequest, validationErrors)
                        }
                    }
                }

                get {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    logWithMDC("userId" to userId.value) {
                        if (userId.isRegistered()) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.Forbidden)
                        }
                    }
                }
            }
        }
    }
}