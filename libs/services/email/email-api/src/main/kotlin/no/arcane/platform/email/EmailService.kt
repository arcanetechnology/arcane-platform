package no.arcane.platform.email

interface EmailService {
    fun sendEmail(
        from: String,
        to: String,
        subject: String,
        body: String,
    ): Boolean
}