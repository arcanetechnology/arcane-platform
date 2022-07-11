package no.arcane.platform.app.invest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.arcane.platform.app.invest.InvestService.getAllFunds
import no.arcane.platform.app.invest.InvestService.isApproved
import no.arcane.platform.app.invest.InvestService.saveFundInfoRequest
import no.arcane.platform.app.invest.InvestService.saveStatus
import no.arcane.platform.app.invest.InvestService.sendEmail
import no.arcane.platform.identity.auth.gcp.UserInfo
import no.arcane.platform.user.UserId
import no.arcane.platform.utils.logging.logWithMDC

fun Application.module() {
    routing {
        authenticate("esp-v2-header") {
            route("/apps/invest/funds") {
                post("/{fund-id}") {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    logWithMDC("userId" to userId.value) {
                        val fundId = FundId(
                            call.parameters["fund-id"] ?: throw BadRequestException("Missing fund id")
                        )
                        val fundInfoRequest = call.receive<FundInfoRequest>()
                        val validationErrors = fundInfoRequest.validate()
                        if (validationErrors.isEmpty()) {
                            if (fundInfoRequest.isApproved(fundId = fundId)) {
                                // store
                                userId.saveFundInfoRequest(
                                    fundId = fundId,
                                    fundInfoRequest = fundInfoRequest
                                )
                                userId.saveStatus(
                                    fundId = fundId,
                                    status = Status.REGISTERED,
                                )
                                // email
                                val userEmail = call.principal<UserInfo>()!!.email
                                fundInfoRequest.sendEmail(to = userEmail)
                                // respond
                                call.respond(HttpStatusCode.OK)
                            } else {
                                // store
                                userId.saveStatus(
                                    fundId = fundId,
                                    status = Status.NOT_AUTHORIZED,
                                )
                                // respond
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
                        call.respond(
                            userId
                                .getAllFunds()
                                .map { (fundId, fund) ->
                                    fundId.value to (fund?.status ?: Status.NOT_REGISTERED)
                                }
                                .toMap()
                        )
                    }
                }
            }
        }
    }
}