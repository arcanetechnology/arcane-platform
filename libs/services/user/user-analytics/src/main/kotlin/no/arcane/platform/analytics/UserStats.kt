package no.arcane.platform.analytics

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ListUsersPage
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import no.arcane.platform.google.coroutine.ktx.await
import java.io.FileReader
import java.io.FileWriter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal

@Serializable
data class User(
    val email: String,
    val idProviders: List<String>,
    val createdOn: String,
    val lastSignIn: String,
    val lastActive: String,
)

class UserAnalytics {

    private fun usersTimeline(map: (User) -> String) = FirebaseUsersFetcher()
        .users
        .map(map)
        .groupBy { it }
        .mapValues { (_, values) -> values.count() }
        .toSortedMap()

    fun usersCreatedTimeline() = usersTimeline(User::createdOn)

    fun usersActiveTimeline() = usersTimeline(User::lastActive)

    fun usersByProviders() = FirebaseUsersFetcher()
        .users
        .groupBy { it.idProviders }
        .mapValues { (_, values) -> values.count() }
}

private class FirebaseUsersFetcher {

    val users: List<User>

    init {
        users = runBlocking {
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
                            User(
                                email = user.email,
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

    companion object {
        private val firebaseAuth: FirebaseAuth by lazy {
            val firebaseApp = FirebaseApp.initializeApp("user-analytics")
            FirebaseAuth.getInstance(firebaseApp)
        }
    }
}

private fun exportPlatformUserEmails() {
    // firebase users email export
    val platformUsersEmailList = FirebaseUsersFetcher()
        .users
    println("Platform users count: ${platformUsersEmailList.size}")

    val output = platformUsersEmailList
        .joinToString(separator = "\n", transform = User::email)

    val fileWriter = FileWriter("platform_users_email_exported_${Instant.now().truncatedTo(ChronoUnit.SECONDS)}.csv")
    fileWriter.write(output)
    fileWriter.close()
}

private fun generateSendersList() {
    val platformUsersEmailSet = FileReader("platform_users_email_exported.csv")
        .readLines()
        .toSet()
    println("Platform users count: ${platformUsersEmailSet.size}")

    val legacyUsersEmailSet = FileReader("")
        .readLines()
        .toSet()
    println("Legacy users count: ${legacyUsersEmailSet.size}")

    val sendList = (platformUsersEmailSet -  legacyUsersEmailSet)
    println("Send list count: ${sendList.size}")

    val output = sendList
        .joinToString(separator = "\n")
    val fileWriter = FileWriter("send_list_${Instant.now().truncatedTo(ChronoUnit.SECONDS)}.csv")
    fileWriter.write(output)
    fileWriter.close()
}

fun main() {
    exportPlatformUserEmails()
}