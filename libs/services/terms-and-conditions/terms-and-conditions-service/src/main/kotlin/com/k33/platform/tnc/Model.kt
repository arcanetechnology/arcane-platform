package com.k33.platform.tnc

import kotlinx.serialization.Serializable
import com.k33.platform.user.users

@Serializable
data class Tnc(
    val tncId: String,
    val version: String,
    val accepted: Boolean,
    val spaceId: String,
    val environmentId: String,
    val entryId: String,
    val fieldId: String,
    val timestamp: String,
)

@JvmInline
value class TncId(val value: String) {
    override fun toString(): String = value
}

val termsAndConditions = users.subCollection<Tnc, TncId>("terms-and-conditions")

val history = termsAndConditions.subCollection<Tnc, String>("history")