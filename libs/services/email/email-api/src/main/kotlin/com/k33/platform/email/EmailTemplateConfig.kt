package com.k33.platform.email

data class EmailConfig(
    val email: String,
    val label: String,
)
data class EmailTemplateConfig(
    val from: EmailConfig,
    val sendgridTemplateId: String,
)