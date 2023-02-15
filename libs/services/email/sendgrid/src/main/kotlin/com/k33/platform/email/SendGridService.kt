package com.k33.platform.email

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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.k33.platform.utils.config.loadConfig
import com.k33.platform.utils.logging.getLogger
import kotlinx.serialization.SerialName


typealias K33Email = com.k33.platform.email.Email

object SendGridService : EmailService {

    private val logger by getLogger()

    private val sendGridConfig by loadConfig<SendGridConfig>(name = "sendgrid", path = "sendgrid")


    override suspend fun sendEmail(
        from: K33Email,
        toList: List<K33Email>,
        ccList: List<K33Email>,
        bccList: List<K33Email>,
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

        fun K33Email.toSendgridEmail(): Email {
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
                        toList.map(K33Email::toSendgridEmail).forEach(this::addTo)
                        ccList.map(K33Email::toSendgridEmail).forEach(this::addCc)
                        bccList.map(K33Email::toSendgridEmail).forEach(this::addBcc)
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

    suspend fun upsertMarketingContacts(
        contactEmails: List<String>,
        contactListIds: List<String> = emptyList(),
    ): Boolean {

        @Serializable
        data class Contact(
            val email: String,
        )

        @Serializable
        data class UpsertContactsRequest(
            @SerialName("list_ids") val listIds: List<String>,
            val contacts: List<Contact>
        )

        return coroutineScope {
            val sendGrid = SendGrid(sendGridConfig.apiKey)
            contactEmails
                // https://docs.sendgrid.com/api-reference/contacts/add-or-update-a-contact
                // max limit of 30k contacts in email request
                .chunked(30_000)
                .map { emailStringList ->
                    async {
                        try {
                            val upsertContactsRequest = UpsertContactsRequest(
                                listIds = contactListIds,
                                contacts = emailStringList.map(::Contact),
                            )
                            val jsonBody = Json.encodeToString(upsertContactsRequest)
                            val request = Request().apply {
                                method = Method.PUT
                                endpoint = "/marketing/contacts"
                                this.body = jsonBody
                            }
                            val response = withContext(Dispatchers.IO) {
                                sendGrid.api(request)
                            }
                            (response.statusCode in 200..299)
                        } catch (e: Exception) {
                            logger.error("Failed to upsert contacts", e)
                            false
                        }
                    }
                }
                .awaitAll()
                .all { it }
        }
    }
}