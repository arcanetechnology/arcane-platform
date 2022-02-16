package no.arcane.platform.utils.graphql

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userId: String,
    val analyticsId: String,
)

@Serializable
data class Tnc(
    val tncId: String,
    val version: String,
    val accepted: Boolean,
    val spaceId: String,
    val entryId: String,
    val fieldId: String,
    val timestamp: String,
)
