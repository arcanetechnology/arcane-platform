package no.arcane.platform.email

interface EmailService {
    suspend fun sendEmail(
        from: String,
        to: String,
        subject: String,
        contentType: ContentType,
        body: String,
    ): Boolean
}

enum class ContentType {
    HTML,
    PLAIN_TEXT,
    MONOSPACE_TEXT,
}