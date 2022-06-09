package no.arcane.platform.app.trade.admin.api.rest

import io.ktor.http.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import no.arcane.platform.app.trade.ledger.db.spanner.Ledger
import no.arcane.platform.app.trade.ledger.db.spanner.user.UserStore
import no.arcane.platform.app.trade.ledger.db.spanner.User as DbUser

fun Route.users() {
    route("users/{userId}") {
        // register user
        post {
            Ledger
                .registerUser(userId = userId())
                .map(DbUser::toUser)
                .thenRespond(ifError = HttpStatusCode.BadRequest)
        }
        // get user
        get {
            UserStore
                .get(userId = userId())
                .map(DbUser::toUser)
                .thenRespond(ifError = HttpStatusCode.NotFound)
        }
        profiles()
    }
}

@Serializable
data class User(
    val id: String,
    val createdOn: String,
)

fun DbUser.toUser() = User(
    id = userId.value,
    createdOn = createdOn.toString(),
)