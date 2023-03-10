package com.k33.platform.email

interface EmailService {
    suspend fun sendEmail(
        from: Email,
        toList: List<Email>,
        ccList: List<Email> = emptyList(),
        bccList: List<Email> = emptyList(),
        mail: Mail,
    ): Boolean

    suspend fun upsertMarketingContacts(
        contactEmails: List<String>,
        contactListIds: List<String>,
    ): Boolean

    suspend fun unlistMarketingContacts(
        contactEmails: List<String>,
        contactListId: String,
    ): Boolean
}

sealed class Mail

class MailContent(
    val subject: String,
    val contentType: ContentType,
    val body: String,
) : Mail()

class MailTemplate(
    val templateId: String,
) : Mail()

enum class ContentType {
    HTML,
    PLAIN_TEXT,
    MONOSPACE_TEXT,
}

data class Email(
    val address: String,
    val label: String? = null,
) {
    override fun toString() = "$label <$address>"
}