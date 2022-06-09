package no.arcane.platform.tests.trade

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import kotlinx.serialization.Serializable
import java.util.*

enum class ProfileType {
    PERSONAL,
    BUSINESS,
}

@Serializable
data class Profile(
    val id: String,
    val alias: String,
    val type: ProfileType,
    val createdOn: String,
    val updatedOn: String,
)


private suspend fun getProfiles(userId: String): List<Profile> =
    get(path = "apps/trade-admin/users/${userId}/profiles").body()

private suspend fun getProfile(
    userId: String,
    profileId: String
): Profile = get(path = "apps/trade-admin/users/${userId}/profiles/${profileId}").body()

@Serializable
data class AddProfile(
    val alias: String,
    val type: ProfileType,
)

suspend fun addProfile(userId: String, addProfile: AddProfile): Profile =
    post(
        path = "apps/trade-admin/users/${userId}/profiles",
        body = addProfile,
    ).body()

fun BehaviorSpec.profileTests() {
    given("user is registered to trade app") {
        val userId = UUID.randomUUID().toString()
        registerUser(userId = userId)
        `when`("GET /apps/trade-admin/users/{userId}/profiles") {
            val profiles = getProfiles(userId = userId)
            then("profiles should be empty") {
                profiles shouldBe emptyList()
            }
        }
        `when`("POST /apps/trade-admin/users/{userId}/profiles") {
            val addProfile = AddProfile(
                alias = "Test Profile",
                type = ProfileType.PERSONAL,
            )
            val profile: Profile = addProfile(
                userId = userId,
                addProfile = addProfile,
            )
            then("profile should be added") {
                profile.alias shouldBe addProfile.alias
                profile.type shouldBe addProfile.type
            }
            then("GET /apps/trade-admin/users/{userId}/profiles => [profile]") {
                getProfiles(userId = userId) shouldBe listOf(profile)
            }
            then("GET /apps/trade-admin/users/{userId}/profiles/{profileId} => profile") {
                getProfile(
                    userId = userId,
                    profileId = profile.id,
                ) shouldBe profile
            }
        }
    }
}