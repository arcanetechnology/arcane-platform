package no.arcane.platform.user

import io.firestore4k.typed.div
import io.firestore4k.typed.get
import io.firestore4k.typed.put
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.arcane.platform.identity.auth.gcp.UserInfo
import no.arcane.platform.user.UserService.createUser
import no.arcane.platform.user.UserService.fetchUser
import no.arcane.platform.utils.logging.getLogger
import java.util.*

fun Application.module() {

    val logger by getLogger()

    routing {
        authenticate("esp-v2-header") {
            route("/user") {
                get {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    val user = userId.fetchUser()
                    if (user != null) {
                        logger.info("Found user: {}", user)
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        logger.info("User: {} does not exists", userId)
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                post {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    logger.info("Creating user: {}", userId)
                    val user = userId.createUser()
                    if (user == null) {
                        logger.error("Failed to create a user: $userId")
                        call.respond(HttpStatusCode.InternalServerError)
                    } else {
                        call.respond(HttpStatusCode.OK, user)
                    }
                }
            }
        }
    }
}

object UserService {

    suspend fun UserId.createUser(): User? {
        if (doesNotExist()) {
            put(
                users / this,
                User(
                    userId = value,
                    analyticsId = UUID.randomUUID().toString()
                )
            )
        }
        return fetchUser()
    }

    suspend fun UserId.fetchUser(): User? = get(users / this)

    private suspend fun UserId.doesNotExist() = fetchUser() == null
}
