package com.k33.platform.tests

import com.k33.platform.identity.auth.FirebaseIdTokenPayload
import com.k33.platform.identity.auth.gcp.FirebaseAuthService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.util.*


class AuthTest : StringSpec({

    "Parse gcp esp v2 headers" {
        val userId = UUID.randomUUID().toString()
        val firebaseIdTokenPayload: FirebaseIdTokenPayload = apiClient.get {
            url { path("whoami") }
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }.body()
        firebaseIdTokenPayload shouldBe FirebaseIdTokenPayload(subject = userId)
    }

    "Login with Apple ID" {
        val userId = UUID.randomUUID().toString()
        val firebaseCustomToken: String = apiClient.get {
            url { path("firebase-custom-token") }
            headers {
                appendAppleIdToken(userId)
            }
        }.bodyAsText()
        val uid = FirebaseAuthService.createOrMergeUser(
            email = "test@k33.com",
            displayName = "Test User",
        )
        verifyFirebaseCustomToken(
            uid = uid,
            email = "test@k33.com",
            firebaseCustomToken = firebaseCustomToken,
        )
    }
})
