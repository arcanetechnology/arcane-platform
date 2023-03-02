package com.k33.platform.user

data class EmailConfig(
    val email: String,
    val label: String,
)

data class WelcomeEmail(
    val from: EmailConfig,
    val sendgridTemplateId: String,
)