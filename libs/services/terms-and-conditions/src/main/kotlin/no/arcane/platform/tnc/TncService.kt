package no.arcane.platform.tnc

import io.firestore4k.typed.add
import io.firestore4k.typed.div
import io.firestore4k.typed.get
import io.firestore4k.typed.put
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import no.arcane.platform.cms.getCmsService
import no.arcane.platform.email.getEmailService
import no.arcane.platform.identity.auth.gcp.UserInfo
import no.arcane.platform.tnc.TncService.getTnc
import no.arcane.platform.tnc.TncService.setTnc
import no.arcane.platform.user.UserId
import no.arcane.platform.user.users
import no.arcane.platform.utils.logging.getLogger
import java.time.ZoneOffset
import java.time.ZonedDateTime

fun Application.module() {

    val logger by getLogger()

    routing {
        authenticate("esp-v2-header") {
            route("/tnc/{tncId}") {

                post {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    val tncId = TncId(call.parameters["tncId"]!!)
                    val tnc = call.receive<TncRequest>()
                    val savedTnc = userId.setTnc(
                        tncId = tncId,
                        tncRequest = tnc,
                    )
                    if (savedTnc != null) {
                        call.respond(HttpStatusCode.Created, savedTnc)
                    } else {
                        logger.error("Failed to store tnc")
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                }

                get {
                    val userId = UserId(call.principal<UserInfo>()!!.userId)
                    val tncId = TncId(call.parameters["tncId"]!!)
                    val tnc = userId.getTnc(
                        tncId = tncId,
                    )
                    if (tnc != null) {
                        call.respond(HttpStatusCode.OK, tnc)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                post("/email") {
                    val userEmail = call.principal<UserInfo>()!!.email
                    val tncId = TncId(call.parameters["tncId"]!!)
                    val emailSent = TncService.emailTnc(
                        email = userEmail,
                        tncId = tncId,
                    )
                    if (emailSent) {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
    }
}

@Serializable
data class TncRequest(
    val version: String,
    val accepted: Boolean,
    val spaceId: String,
    val entryId: String,
    val fieldId: String,
)

object TncService {

    private val logger by getLogger()

    private val emailService by getEmailService()
    private val cmsService by getCmsService()

    suspend fun UserId.setTnc(
        tncId: TncId,
        tncRequest: TncRequest,
    ): Tnc? {
        cmsService.check(
            entryKey = tncId.value,
            spaceId = tncRequest.spaceId,
            entryId = tncRequest.entryId,
            fieldId = tncRequest.fieldId,
            version = tncRequest.version,
        )
        val tnc = Tnc(
            tncId = tncId.value,
            version = tncRequest.version,
            accepted = tncRequest.accepted,
            spaceId = tncRequest.spaceId,
            entryId = tncRequest.entryId,
            fieldId = tncRequest.fieldId,
            timestamp = ZonedDateTime.now(ZoneOffset.UTC).toString()
        )
        put(users / this / termsAndConditions / tncId, tnc)
        add(users / this / termsAndConditions / tncId / history, tnc)
        return getTnc(tncId)
    }

    suspend fun UserId.getTnc(
        tncId: TncId,
    ): Tnc? = get(users / this / termsAndConditions / tncId)

    fun emailTnc(
        email: String,
        tncId: TncId,
    ): Boolean {
        val html = cmsService.getHtml(entryKey = tncId.value)

        if (html.isNullOrBlank()) {
            logger.error("CMS has no entry for $tncId")
            return false
        }

        return emailService.sendEmail(
            from = "do-not-reply@arcane.no",
            to = email,
            subject = "Privacy Policy",
            body = html,
        )
    }
}