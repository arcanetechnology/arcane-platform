package no.arcane.platform.email

fun getEmailService(): Lazy<EmailService> = lazy { SendGridService }