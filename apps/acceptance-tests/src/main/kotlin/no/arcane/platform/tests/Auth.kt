package no.arcane.platform.tests

import io.kotest.common.runBlocking
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.arcane.platform.identity.auth.AppleIdTokenPayload
import no.arcane.platform.identity.auth.FirebaseIdTokenPayload
import java.util.*

private val jsonPrinter = Json {
    prettyPrint = true
    encodeDefaults = true
}

private val oauthProviderEmulatorClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
    defaultRequest {
        host = "oauth2-provider-emulator"
        port = 8080
    }
}

@OptIn(InternalAPI::class)
fun HeadersBuilder.appendEndpointsApiUserInfoHeader(subject: String) {
    val firebaseIdTokenPayload = FirebaseIdTokenPayload(subject = subject)
    if (System.getenv("BACKEND_HOST") == "test.api.arcane.no") {
        val idToken: String = runBlocking {
            oauthProviderEmulatorClient.get {
                url(path = "firebase-id-token")
                contentType(ContentType.Application.Json)
                setBody(firebaseIdTokenPayload)
            }.body()
        }
        append("Authorization", "Bearer $idToken")
    } else {
        append("X-Endpoint-API-UserInfo",
            jsonPrinter.encodeToString(firebaseIdTokenPayload)
                .toByteArray()
                .let(Base64.getEncoder()::encodeToString)
        )
    }
}

@OptIn(InternalAPI::class)
fun HeadersBuilder.appendAppleIdToken(subject: String) {
    val appleIdTokenPayload = AppleIdTokenPayload(subject = subject)
    if (System.getenv("BACKEND_HOST") == "test.api.arcane.no") {
        val idToken: String = runBlocking {
            oauthProviderEmulatorClient.get {
                url(path = "apple-id-token")
                contentType(ContentType.Application.Json)
                setBody(appleIdTokenPayload)
            }.body()
        }
        append("Authorization", "Bearer $idToken")
    } else {
        append("X-Endpoint-API-UserInfo",
            jsonPrinter.encodeToString(appleIdTokenPayload)
                .toByteArray()
                .let(Base64.getEncoder()::encodeToString)
        )
    }
}