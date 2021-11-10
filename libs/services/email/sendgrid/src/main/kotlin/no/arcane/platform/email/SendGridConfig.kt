package no.arcane.platform.email

data class SendGridConfig(
    val enabled: Boolean,
    val apiKey: String,
)