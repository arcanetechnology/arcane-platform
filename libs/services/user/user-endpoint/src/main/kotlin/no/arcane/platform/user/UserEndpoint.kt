package no.arcane.platform.user

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.arcane.platform.identity.auth.gcp.UserInfo
import no.arcane.platform.user.UserService.createUser
import no.arcane.platform.user.UserService.fetchUser

fun Application.module() {

    routing {
        authenticate("esp-v2-header") {
            route("/user") {
                get {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    val user = userId.fetchUser()
                    if (user != null) {
                        log.info("Found user: {}", user)
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        log.info("User: {} does not exists", userId)
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                post {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    log.info("Creating user: {}", userId)
                    val user = userId.createUser()
                    if (user == null) {
                        log.error("Failed to create a user: $userId")
                        call.respond(HttpStatusCode.InternalServerError)
                    } else {
                        call.respond(HttpStatusCode.OK, user)
                    }
                }
            }
        }
    }
}