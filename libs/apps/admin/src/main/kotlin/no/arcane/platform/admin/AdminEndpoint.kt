package no.arcane.platform.admin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.arcane.platform.user.User
import no.arcane.platform.user.UserId
import no.arcane.platform.user.UserService.fetchUser

fun Application.module() {
    routing {
        authenticate("admin-auth") {
            route("/apps/admin") {
                // search user by email
                // GET /apps/admin/user?email={email}
                get("user") {
                    val email = call.request.queryParameters["email"]
                        ?: throw BadRequestException("Query parameter 'email' is mandatory")
                    val userId = UserAdminService.getUserId(email)
                    if (userId == null) {
                        // user not found in Firebase Auth / Identity Platform
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        call.respond(userId.value)
                        // TODO enable checking registered user in Firestore
                        /*
                        val user = userId.fetchUser()
                        if (user == null) {
                            // user not found in firestore /users/{userId}
                            // user not registered in platform.
                            call.respond(HttpStatusCode.NotFound)
                        } else {
                            // user registered in platform.
                            call.respond(user)
                        }
                        */
                    }
                }
                // search user by id
                // GET /apps/admin/users/{userId}
                get("users/{user-id}") {
                    val userId = UserId(
                        call.parameters["userId"]
                            ?: throw BadRequestException("Missing mandatory parameter 'userId'")
                    )
                    if (UserAdminService.getUser(userId) == null) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        call.respond(userId.value)
                        // TODO enable checking registered user in Firestore
                        /*
                        val user = userId.fetchUser()
                        if (user == null) {
                            // user not found in firestore /users/{userId}
                            // user not registered in platform.
                            call.respond(HttpStatusCode.NotFound)
                        } else {
                            // user registered in platform.
                            call.respond(user)
                        }
                        */
                    }
                }
            }
        }
    }
}


