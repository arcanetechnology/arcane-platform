package no.arcane.platform.tnc

import io.firestore4k.typed.add
import io.firestore4k.typed.div
import io.firestore4k.typed.get
import io.firestore4k.typed.put
import no.arcane.platform.cms.getCmsService
import no.arcane.platform.email.getEmailService
import no.arcane.platform.user.UserId
import no.arcane.platform.user.users
import no.arcane.platform.utils.logging.getLogger
import java.awt.GraphicsEnvironment
import java.time.ZoneOffset
import java.time.ZonedDateTime

object TncService {

    private val logger by getLogger()

    private val emailService by getEmailService()
    private val cmsService by getCmsService()

    suspend fun UserId.setTnc(
        tncId: TncId,
        version: String,
        accepted: Boolean,
        spaceId: String,
        environmentId: String,
        entryId: String,
        fieldId: String,
    ): Tnc? {
        cmsService.check(
            entryKey = tncId.value,
            spaceId = spaceId,
            environmentId = environmentId,
            entryId = entryId,
            fieldId = fieldId,
            version = version,
        )
        val tnc = Tnc(
            tncId = tncId.value,
            version = version,
            accepted = accepted,
            spaceId = spaceId,
            environmentId = environmentId,
            entryId = entryId,
            fieldId = fieldId,
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