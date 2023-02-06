package com.k33.platform.identity.auth.gcp

import com.google.firebase.ErrorCode
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import com.k33.platform.google.coroutine.ktx.await
import com.k33.platform.utils.logging.getLogger

object FirebaseAuthService {

    private val logger by getLogger()

    private val firebaseAuth by lazy {
        val firebaseApp = FirebaseApp.initializeApp("auth")
        FirebaseAuth.getInstance(firebaseApp)
    }

    suspend fun createOrMergeUser(
        email: String,
        displayName: String,
    ): String {
        return findUserIdOrNull(
            email = email
        ) ?: try {
            createUser(
                email = email,
                displayName = displayName,
            ).also {
                logger.info("User created.")
            }
        } catch (e: FirebaseAuthException) {
            if (e.errorCode == ErrorCode.ALREADY_EXISTS) {
                findUserId(email = email).also {
                    logger.info("Found user on 2nd attempt.")
                }
            } else {
                throw e
            }
        }
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

    suspend fun findUserId(
        email: String
    ): String {
        return firebaseAuth.getUserByEmailAsync(email)
            .await()
            .uid
            .also {
                logger.info("Found user.")
            }
    }

    suspend fun findUserIdOrNull(
        email: String
    ): String? {
        return try {
            findUserId(email = email)
        } catch (e: FirebaseAuthException) {
            if (e.errorCode == ErrorCode.NOT_FOUND) {
                logger.info("User not found.")
                return null
            } else {
                throw e
            }
        }
    }

    private suspend fun createUser(
        email: String,
        displayName: String,
    ): String {
        logger.info("Creating new user.")
        val createRequest = UserRecord.CreateRequest()
            .setEmail(email)
            .setEmailVerified(true)
            .setDisplayName(displayName)

        return firebaseAuth
            .createUserAsync(createRequest)
            .await()
            .uid
    }
}