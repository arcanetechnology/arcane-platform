package com.k33.platform.email

import io.kotest.core.spec.style.StringSpec

class EmailServiceTest : StringSpec({

    "send email".config(enabled = false) {
        val emailService by getEmailService()
        emailService.sendEmail(
            from = Email("vihang@k33.com"),
            toList = listOf(Email("vihang@k33.com")),
            mail = MailContent(
                subject = "Test Email",
                contentType = ContentType.MONOSPACE_TEXT,
                body = "This is a test email",
            )
        )
    }

    "send email using template".config(enabled = false) {
        val emailService by getEmailService()
        emailService.sendEmail(
            from = Email("vihang@k33.com"),
            toList = listOf(Email("vihang@k33.com")),
            mail = MailTemplate(
                templateId = ""
            )
        )
    }
})