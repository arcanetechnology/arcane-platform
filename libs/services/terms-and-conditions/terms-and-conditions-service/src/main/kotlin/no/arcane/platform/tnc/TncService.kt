package no.arcane.platform.tnc

import io.firestore4k.typed.add
import io.firestore4k.typed.div
import io.firestore4k.typed.get
import io.firestore4k.typed.put
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import no.arcane.platform.cms.LegalEntryMetadata
import no.arcane.platform.cms.getCmsService
import no.arcane.platform.email.ContentType
import no.arcane.platform.email.Email
import no.arcane.platform.email.getEmailService
import no.arcane.platform.user.UserId
import no.arcane.platform.user.users
import no.arcane.platform.utils.logging.getLogger
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
        return coroutineScope {
            launch {
                cmsService.check(
                    LegalEntryMetadata(
                        id = tncId.value,
                        version = version,
                        spaceId = spaceId,
                        environmentId = environmentId,
                        entryId = entryId,
                        fieldId = fieldId,
                    )
                )
            }
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
            put(users / this@setTnc / termsAndConditions / tncId, tnc)
            add(users / this@setTnc / termsAndConditions / tncId / history, tnc)
            getTnc(tncId)
        }
    }

    suspend fun UserId.getTnc(
        tncId: TncId,
    ): Tnc? = get(users / this / termsAndConditions / tncId)

    suspend fun emailTnc(
        email: String,
        tncId: TncId,
    ): Boolean {
        val html = cmsService.getHtml(id = tncId.value)

        if (html.isNullOrBlank()) {
            logger.error("CMS has no entry for $tncId")
            return false
        }

        return emailService.sendEmail(
            from = Email(address = "do-not-reply@arcane.no", label = "Arcane"),
            toList = listOf(Email(email)),
            subject = "Privacy Policy",
            contentType = ContentType.HTML,
            body = html,
        )
    }
}