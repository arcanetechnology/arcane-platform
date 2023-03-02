package com.k33.platform.user

import io.firestore4k.typed.div
import io.firestore4k.typed.get
import io.firestore4k.typed.put
import java.util.*

object UserService {

    suspend fun UserId.createUser(email: String): User? {
        if (doesNotExist()) {
            put(
                users / this,
                User(
                    userId = value,
                    analyticsId = UUID.randomUUID().toString()
                )
            )
            UserEventHandler.onNewUserCreated(email = email)
        }
        return fetchUser()
    }

    suspend fun UserId.fetchUser(): User? = get(users / this)

    private suspend fun UserId.doesNotExist() = fetchUser() == null
}