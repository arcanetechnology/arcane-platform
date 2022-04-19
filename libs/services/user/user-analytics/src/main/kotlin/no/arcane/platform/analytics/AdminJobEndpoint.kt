package no.arcane.platform.analytics

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.arcane.platform.filestore.FileStoreService

fun Application.module() {

    routing {
        route("/admin/jobs/update-firebase-users-stats") {
            post {
                updateFirebaseUsersStats()
                call.respond("")
            }
        }
    }
}

fun updateFirebaseUsersStats() {
    val userAnalytics = UserAnalytics()
    val csvFileContents = buildString {
        appendLine("""DATE, USER_CREATED_COUNT""")
        userAnalytics.usersCreatedTimeline().forEach { (time, count) ->
            appendLine(""""$time", $count""")
        }
    }
    FileStoreService.upload(
        fileId = "user-created-timeline",
        content = csvFileContents.toByteArray()
    )
}