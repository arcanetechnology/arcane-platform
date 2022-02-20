package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import no.arcane.platform.identity.auth.FirebaseIdTokenPayload
import no.arcane.platform.identity.auth.gcp.FirebaseAuthService
import java.util.*


class AuthTest : StringSpec({

    "Parse gcp esp v2 headers" {
        val userId = UUID.randomUUID().toString()
        val firebaseIdTokenPayload = apiClient.get<FirebaseIdTokenPayload> {
            url { path("whoami") }
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }
        firebaseIdTokenPayload shouldBe FirebaseIdTokenPayload(subject = userId)
    }

    "Login with Apple ID" {
        val userId = UUID.randomUUID().toString()
        val firebaseCustomToken = apiClient.get<String> {
            url { path("firebase-custom-token") }
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }
        val uid = FirebaseAuthService.createOrMergeUser(
            email = "test@arcane.no",
            displayName = "Test User",
        )
        verifyFirebaseCustomToken(
            uid = uid,
            email = "test@arcane.no",
            firebaseCustomToken = firebaseCustomToken,
        )
    }
})
