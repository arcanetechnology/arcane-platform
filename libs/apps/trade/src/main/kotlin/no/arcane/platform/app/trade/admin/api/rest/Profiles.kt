package no.arcane.platform.app.trade.admin.api.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import no.arcane.platform.app.trade.ledger.db.spanner.Ledger
import no.arcane.platform.app.trade.ledger.db.spanner.ProfileType
import no.arcane.platform.app.trade.ledger.db.spanner.profile.ProfileStore
import no.arcane.platform.app.trade.ledger.db.spanner.Profile as DbProfile

fun Route.profiles() {
    route("profiles") {
        // add profile
        post {
            val request = call.receive<AddProfile>()
            Ledger
                .addProfile(
                    userId = userId(),
                    alias = request.alias,
                    type = request.type,
                )
                .map(DbProfile::toProfile)
                .thenRespond(ifError = HttpStatusCode.BadRequest)
        }
        // get all profiles
        get {
            ProfileStore
                .getAll(
                    userId = userId()
                )
                .map { it.map(DbProfile::toProfile) }
                .thenRespond(ifError = HttpStatusCode.NotFound)
        }
        route("{profileId}") {
            // get profile
            get {
                ProfileStore
                    .get(
                        profileId = profileId()
                    )
                    .map(DbProfile::toProfile)
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
            // update profile
            put {
                Ledger
                    .updateProfile(
                        profileId = profileId(),
                        alias = call.receive<Alias>().alias,
                    )
                    .map(DbProfile::toProfile)
                    .thenRespond(ifError = HttpStatusCode.NotFound)
            }
            // delete profile
            delete {
                TODO("Safely delete profile")
            }
            fiatAccounts()
            cryptoAccounts()
        }
    }
}

@Serializable
data class Profile(
    val id: String,
    val alias: String,
    val type: ProfileType,
    val createdOn: String,
    val updatedOn: String,
)

@Serializable
data class AddProfile(
    val alias: String,
    val type: ProfileType,
)

fun DbProfile.toProfile() = Profile(
    id = profileId.value,
    alias = alias,
    type = type,
    createdOn = createdOn.toString(),
    updatedOn = updatedOn.toString(),
)

