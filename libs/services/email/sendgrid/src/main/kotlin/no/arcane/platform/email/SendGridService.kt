package no.arcane.platform.email

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.MailSettings
import com.sendgrid.helpers.mail.objects.Setting
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.getLogger

object SendGridService : EmailService {

    private val logger by getLogger()

    private val sendGridConfig by loadConfig<SendGridConfig>(name = "sendgrid", path = "sendgrid")

    override fun sendEmail(
        from: String,
        to: String,
        subject: String,
        body: String,
    ): Boolean {
        try {
            val mail = Mail(
                Email(from),
                subject,
                Email(to),
                Content("text/html", body)
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
            val response = sendGrid.api(request)
            return (response.statusCode in 200..299)

        } catch (e: Exception) {
            logger.error("Failed to send email", e)
            return false
        }
    }
}