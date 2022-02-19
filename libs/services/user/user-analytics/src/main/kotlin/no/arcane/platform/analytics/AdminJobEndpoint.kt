package no.arcane.platform.analytics

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.arcane.platform.filestore.FileStoreService

fun Application.module() {

    routing {
        route("/admin/jobs/update-firebase-users-stats") {
            post {
                updateFirebaseUsersStats()
                call.respond(HttpStatusCode.OK)
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