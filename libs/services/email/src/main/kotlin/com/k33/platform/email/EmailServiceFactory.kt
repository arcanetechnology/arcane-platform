package com.k33.platform.email

fun getEmailService(): Lazy<EmailService> = lazy { SendGridService }