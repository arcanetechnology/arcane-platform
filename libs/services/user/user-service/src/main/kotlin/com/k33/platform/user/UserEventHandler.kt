package com.k33.platform.user

import com.k33.platform.email.Email
import com.k33.platform.email.MailTemplate
import com.k33.platform.email.getEmailService
import com.k33.platform.utils.config.loadConfig
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object UserEventHandler {

    private val emailService by getEmailService()

    private val welcomeEmail by loadConfig<WelcomeEmail>(
        "userService",
        "service.user.welcomeEmail"
    )

    private val researchWelcomeEmail by loadConfig<WelcomeEmail>(
        "userService",
        "service.user.researchWelcomeEmail"
    )

    suspend fun onNewUserCreated(
        email: String
    ) {
        coroutineScope {
            launch {
                emailService.sendEmail(
                    from = Email(
                        address = welcomeEmail.from.email,
                        label = welcomeEmail.from.label,
                    ),
                    toList = listOf(Email(email)),
                    mail = MailTemplate(welcomeEmail.sendgridTemplateId)
                )
            }
            launch {
                emailService.sendEmail(
                    from = Email(
                        address = researchWelcomeEmail.from.email,
                        label = researchWelcomeEmail.from.label,
                    ),
                    toList = listOf(Email(email)),
                    mail = MailTemplate(researchWelcomeEmail.sendgridTemplateId)
                )
            }
        }
    }
}