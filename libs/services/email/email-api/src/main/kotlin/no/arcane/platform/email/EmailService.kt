package no.arcane.platform.email

interface EmailService {
    suspend fun sendEmail(
        from: Email,
        toList: List<Email>,
        ccList: List<Email> = emptyList(),
        bccList: List<Email> = emptyList(),
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

data class Email(
    val address: String,
    val label: String? = null,
)