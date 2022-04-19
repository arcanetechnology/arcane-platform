package no.arcane.platform.analytics

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ListUsersPage
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
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

            val users = flow {
                var localPage: ListUsersPage? = page
                while (localPage != null) {
                    for (user in localPage.values) {
                        emit(
                            UserMetadata(
                                idProviders = user.providerData.map { it.providerId },
                                createdOn = Instant.ofEpochMilli(user.userMetadata.creationTimestamp),
                                lastSignIn = Instant.ofEpochMilli(user.userMetadata.lastSignInTimestamp),
                                lastActive = Instant.ofEpochMilli(user.userMetadata.lastRefreshTimestamp),
                            )
                        )
                    }
                    localPage = localPage.nextPage
                }
            }
            users.toList()
        }
    }

    private fun usersTimeline(map: (UserMetadata) -> Instant) = userMetadataList
        .map(map)
        .map { it.atZone(ZoneId.of("UTC")) }
        .groupBy { it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
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

private data class UserMetadata(
    val idProviders: List<String>,
    val createdOn: Instant,
    val lastSignIn: Instant,
    val lastActive: Instant,
)