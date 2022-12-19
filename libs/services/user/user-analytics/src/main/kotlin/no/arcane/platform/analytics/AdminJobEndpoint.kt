package no.arcane.platform.analytics

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.arcane.platform.filestore.FileStoreService
import kotlin.math.roundToInt

fun Application.module() {

    routing {
        route("/admin/jobs") {
            post("update-firebase-users-stats") {
                updateFirebaseUsersStats()
                call.respond(HttpStatusCode.OK)
            }
            put("sync-sendgrid-contacts") {
                SendgridContactsSync.syncSendgridContacts()
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

suspend fun updateFirebaseUsersStats() {
    val userAnalytics = UserAnalytics()
    val csvFileContents = buildString {
        appendLine("""DATE, USER_CREATED_COUNT, MOVING_AVG_LAST_SEVEN""")
        val userCountList = mutableListOf<Int>()
        userAnalytics.usersCreatedTimeline().forEach { (time, count) ->
            userCountList += count
            appendLine(""""$time", $count, ${userCountList.takeLast(7).average().roundToInt()}""")
        }
    }
    FileStoreService.upload(
        fileId = "user-created-timeline",
        content = csvFileContents.toByteArray()
    )
}