package no.arcane.platform.email

import io.kotest.core.spec.style.StringSpec

class EmailServiceTest : StringSpec({

    "send email".config(enabled = false) {
        val emailService by getEmailService()
        emailService.sendEmail(
            from = Email("vihang@arcane.no"),
            toList = listOf(Email("vihang@arcane.no")),
            subject = "Test Email",
            contentType = ContentType.MONOSPACE_TEXT,
            body = "This is a test email"
        )
    }
})