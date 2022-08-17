package no.arcane.platform.email

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.MailSettings
import com.sendgrid.helpers.mail.objects.Personalization
import com.sendgrid.helpers.mail.objects.Setting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.getLogger


typealias ArcaneEmail = no.arcane.platform.email.Email

object SendGridService : EmailService {

    private val logger by getLogger()

    private val sendGridConfig by loadConfig<SendGridConfig>(name = "sendgrid", path = "sendgrid")


    override suspend fun sendEmail(
        from: ArcaneEmail,
        toList: List<ArcaneEmail>,
        ccList: List<ArcaneEmail>,
        bccList: List<ArcaneEmail>,
        subject: String,
        contentType: ContentType,
        body: String
    ): Boolean {

        logger.debug(
            """

            from: $from
            to: ${toList.joinToString()}
            cc: ${ccList.joinToString()}
            bcc: ${bccList.joinToString()}
            subject: $subject

            """.trimIndent() + body
        )

        fun ArcaneEmail.toSendgridEmail(): Email {
            return if (label.isNullOrBlank()) {
                Email(address)
            } else {
                Email(address, label)
            }
        }

        try {
            val content = when (contentType) {
                ContentType.HTML -> Content("text/html", body)
                ContentType.PLAIN_TEXT -> Content("text/plain", body)
                ContentType.MONOSPACE_TEXT -> Content(
                    "text/html",
                    buildString {
                        appendHTML().div {
                            style = "font-family: monospace;"
                            for (line in body.lines()) {
                                +line
                                br()
                            }
                        }
                    }
                )
            }

            val mail = Mail().apply {
                this.from = from.toSendgridEmail()
                this.subject = subject
                addPersonalization(
                    Personalization().apply {
                        toList.map(ArcaneEmail::toSendgridEmail).forEach(this::addTo)
                        ccList.map(ArcaneEmail::toSendgridEmail).forEach(this::addCc)
                        bccList.map(ArcaneEmail::toSendgridEmail).forEach(this::addBcc)
                    }
                )
                addContent(content)
            }

            if (sendGridConfig.enabled.not()) {
                mail.mailSettings = MailSettings().apply {
                    setSandboxMode(
                        Setting().apply {
                            enable = true
                        }
                    )
                }
            }

            val request = Request().apply {
                method = Method.POST
                endpoint = "mail/send"
                this.body = mail.build()
            }

            val sendGrid = SendGrid(sendGridConfig.apiKey)
            val response = withContext(Dispatchers.IO) {
                sendGrid.api(request)
            }
            return (response.statusCode in 200..299)

        } catch (e: Exception) {
            logger.error("Failed to send email", e)
            return false
        }
    }
}