package no.arcane.platform.identity.auth.gcp

import com.google.firebase.ErrorCode
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import kotlinx.coroutines.runBlocking
import no.arcane.platform.utils.logging.getLogger

object FirebaseAuthService {

    private val logger by getLogger()

    private val firebaseApp = FirebaseApp.initializeApp("auth")
    private val firebaseAuth = FirebaseAuth.getInstance(firebaseApp)

    suspend fun createOrMergeUser(
        email: String,
        displayName: String,
    ): String {
        val userRecord = try {
            firebaseAuth.getUserByEmailAsync(email).await().also {
                logger.info("Found user.")
            }
        } catch (e: FirebaseAuthException) {
            if (e.errorCode == ErrorCode.NOT_FOUND) {
                logger.info("User not found.")
                try {
                    createUser(
                        email = email,
                        displayName = displayName,
                    ).also {
                        logger.info("User created.")
                    }
                } catch (e: FirebaseAuthException) {
                    if (e.errorCode == ErrorCode.ALREADY_EXISTS) {
                        firebaseAuth.getUserByEmailAsync(email).await().also {
                            logger.info("Found user on 2nd attempt.")
                        }
                    } else {
                        throw e
                    }
                }
            } else {
                throw e
            }
        }

        return userRecord.uid
    }

    suspend fun createCustomToken(
        uid: String,
        email: String,
    ): String {
        return firebaseAuth.createCustomTokenAsync(
            uid,
            mapOf(
                "user_id" to uid,
                "email" to email,
                "email_verified" to true,
            )
        ).await()
    }

    private suspend fun createUser(
        email: String,
        displayName: String,
    ): UserRecord {
        logger.info("Creating new user.")
        val createRequest = UserRecord.CreateRequest()
            .setEmail(email)
            .setEmailVerified(true)
            .setDisplayName(displayName)

        return firebaseAuth
            .createUserAsync(createRequest)
            .await()
    }
}