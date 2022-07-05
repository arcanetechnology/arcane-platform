package no.arcane.platform.email

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.MailSettings
import com.sendgrid.helpers.mail.objects.Setting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.getLogger

object SendGridService : EmailService {

    private val logger by getLogger()

    private val sendGridConfig by loadConfig<SendGridConfig>(name = "sendgrid", path = "sendgrid")

    override suspend fun sendEmail(
        from: String,
        to: String,
        subject: String,
        contentType: ContentType,
        body: String,
    ): Boolean {
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
            val mail = Mail(
                Email(from),
                subject,
                Email(to),
                content
            )

            if (sendGridConfig.enabled.not()) {
                mail.mailSettings = MailSettings().apply {
                    setSandboxMode(
                        Setting().apply {
                            enable = true
                        }
                    )
                }
            }

            val request = Request()
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()

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