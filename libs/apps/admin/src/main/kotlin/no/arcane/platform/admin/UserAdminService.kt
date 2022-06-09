package no.arcane.platform.admin

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import no.arcane.platform.google.coroutine.ktx.await
import no.arcane.platform.user.UserId

object UserAdminService {

    suspend fun getUserId(email: String): UserId? {
        return try {
            firebaseAuth
                .getUserByEmailAsync(email)
                .await()
                .uid
                .let(::UserId)
        } catch (e: FirebaseAuthException) {
            null
        }
    }

    suspend fun getUser(userId: UserId): String? {
        return try {
            firebaseAuth
                .getUserAsync(userId.value)
                .await()
                .email
        } catch (e: FirebaseAuthException) {
            null
        }
    }

    private val firebaseAuth: FirebaseAuth by lazy {
        val firebaseApp = FirebaseApp.initializeApp("admin")
        FirebaseAuth.getInstance(firebaseApp)
    }
}