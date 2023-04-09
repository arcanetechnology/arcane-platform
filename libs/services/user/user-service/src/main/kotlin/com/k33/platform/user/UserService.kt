package com.k33.platform.user

import io.firestore4k.typed.FirestoreClient
import io.firestore4k.typed.div
import java.util.*

object UserService {

    private val firestoreClient by lazy { FirestoreClient() }

    suspend fun UserId.createUser(email: String): User? {
        if (doesNotExist()) {
            firestoreClient.put(
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

    suspend fun UserId.fetchUser(): User? = firestoreClient.get(users / this)

    private suspend fun UserId.doesNotExist() = fetchUser() == null
}