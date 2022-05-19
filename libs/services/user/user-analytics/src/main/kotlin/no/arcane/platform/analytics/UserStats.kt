package no.arcane.platform.analytics

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ListUsersPage
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import no.arcane.platform.google.coroutine.ktx.await
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class UserAnalytics {

    private val userMetadataList: List<UserMetadata>

    init {
        userMetadataList = runBlocking {
            val page: ListUsersPage? = firebaseAuth
                .listUsersAsync(null)
                .await()

            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            fun Long.toDateString(): String {
                return Instant
                    .ofEpochMilli(this)
                    .atZone(ZoneId.of("UTC"))
                    .format(dateFormatter)
            }

            val users = flow {
                var localPage: ListUsersPage? = page

                while (localPage != null) {
                    for (user in localPage.values) {
                        emit(
                            UserMetadata(
                                idProviders = user.providerData.map { it.providerId },
                                createdOn = user.userMetadata.creationTimestamp.toDateString(),
                                lastSignIn = user.userMetadata.lastSignInTimestamp.toDateString(),
                                lastActive = user.userMetadata.lastRefreshTimestamp.toDateString(),
                            )
                        )
                    }
                    localPage = localPage.nextPage
                }
            }
            users.toList()
        }
    }

    private fun usersTimeline(map: (UserMetadata) -> String) = userMetadataList
        .map(map)
        .groupBy { it }
        .mapValues { (_, values) -> values.count() }
        .toSortedMap()

    fun usersCreatedTimeline() = usersTimeline(UserMetadata::createdOn)

    fun usersActiveTimeline() = usersTimeline(UserMetadata::lastActive)

    fun usersByProviders() = userMetadataList
        .groupBy { it.idProviders }
        .mapValues { (_, values) -> values.count() }

    companion object {
        private val firebaseAuth: FirebaseAuth by lazy {
            val firebaseApp = FirebaseApp.initializeApp("user-analytics")
            FirebaseAuth.getInstance(firebaseApp)
        }
    }
}

@Serializable
data class UserMetadata(
    val idProviders: List<String>,
    val createdOn: String,
    val lastSignIn: String,
    val lastActive: String,
)