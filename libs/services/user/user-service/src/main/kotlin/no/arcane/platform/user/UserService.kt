package no.arcane.platform.user

import io.firestore4k.typed.div
import io.firestore4k.typed.get
import io.firestore4k.typed.put
import java.util.*

object UserService {

    suspend fun UserId.createUser(): User? {
        if (doesNotExist()) {
            put(
                users / this,
                User(
                    userId = value,
                    analyticsId = UUID.randomUUID().toString()
                )
            )
        }
        return fetchUser()
    }

    suspend fun UserId.fetchUser(): User? = get(users / this)

    private suspend fun UserId.doesNotExist() = fetchUser() == null
}