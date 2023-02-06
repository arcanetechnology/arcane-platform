package com.k33.platform.user

import io.firestore4k.typed.rootCollection
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userId: String,
    val analyticsId: String,
)

@JvmInline
value class UserId(val value: String) {
    override fun toString(): String = value
}

val users = rootCollection<User, UserId>("users")
