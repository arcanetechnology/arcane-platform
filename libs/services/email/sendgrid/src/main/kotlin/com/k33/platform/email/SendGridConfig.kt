package com.k33.platform.email

data class SendGridConfig(
    val enabled: Boolean,
    val apiKey: String,
)