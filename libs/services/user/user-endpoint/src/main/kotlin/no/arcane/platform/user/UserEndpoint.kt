package no.arcane.platform.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.arcane.platform.identity.auth.gcp.UserInfo
import no.arcane.platform.user.UserService.createUser
import no.arcane.platform.user.UserService.fetchUser
import no.arcane.platform.utils.logging.logWithMDC

fun Application.module() {

    routing {
        authenticate("esp-v2-header") {
            route("/user") {
                get {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    logWithMDC("userId" to userId.value) {
                        val user = userId.fetchUser()
                        if (user != null) {
                            call.application.log.info("Found user: {}", user)
                            call.respond(HttpStatusCode.OK, user)
                        } else {
                            call.application.log.info("User: {} does not exists", userId)
                            call.respond(HttpStatusCode.NotFound)
                        }
                    }
                }

                post {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    logWithMDC("userId" to userId.value) {
                        call.application.log.info("Creating user: {}", userId)
                        val user = userId.createUser()
                        if (user == null) {
                            call.application.log.error("Failed to create a user: $userId")
                            call.respond(HttpStatusCode.InternalServerError)
                        } else {
                            call.respond(HttpStatusCode.OK, user)
                        }
                    }
                }
            }
        }
    }
}