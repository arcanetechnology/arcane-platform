package com.k33.platform.analytics

import com.k33.platform.email.SendGridService
import com.k33.platform.filestore.FileStoreService
import com.k33.platform.utils.logging.NotifySlack
import com.k33.platform.utils.logging.getLogger
import com.k33.platform.utils.logging.getMarker
import java.io.File
import java.io.FileWriter
import java.security.MessageDigest
import java.util.*

object SendgridContactsSync {

    private val logger by getLogger()

    suspend fun syncSendgridContacts(
        contactListId: String,
    ) {

        var message = ""

        // fetch users emails
        val platformUsersEmailList = FirebaseUsersFetcher
            .fetchUsers()
            .map(User::email)
            .map(String::lowercase)
        message += "Platform users count: ${platformUsersEmailList.size}"

        val excludeUsersEmailList = FileStoreService
            .download(fileId = "sendgrid-contacts-sync")
            .let(::String)
            .split('\n')
        message += "\nExclude users count: ${excludeUsersEmailList.size}"

        val messageDigest = MessageDigest.getInstance("SHA-256")
        val base64Encoder = Base64.getEncoder()
        val sendList = platformUsersEmailList
            .filter { !excludeUsersEmailList.contains(base64Encoder.encodeToString(messageDigest.digest(it.toByteArray()))) }
        message += "\nSend list count: ${sendList.size}"

        val success = SendGridService.upsertMarketingContacts(
            contactEmails = sendList.toList(),
            contactListIds = listOf(contactListId),
        )
        if (success) {
            logger.info("Synced Sendgrid contact\n$message")
        } else {
            logger.warn(NotifySlack.NOTIFY_SLACK_ALERTS.getMarker(), "Failed to sync Sendgrid Contacts")
        }
    }
}

fun encodeToMessageDigest() {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val base64Encoder = Base64.getEncoder()
    val output = File("")
        .readLines()
        .joinToString(separator = "\n") {
            base64Encoder.encodeToString(messageDigest.digest(it.lowercase().toByteArray()))
        }
    val fileWriter = FileWriter("exclude-contact-emails.csv")
    fileWriter.write(output)
    fileWriter.close()
}