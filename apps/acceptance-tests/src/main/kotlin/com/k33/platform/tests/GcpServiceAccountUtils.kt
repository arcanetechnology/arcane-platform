package com.k33.platform.tests

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.util.X509CertUtils
import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.cert.X509Certificate

val jsonParser = Json {
    ignoreUnknownKeys = true
}

private val jacksonObjectMapper = jacksonObjectMapper()

@Serializable
private data class ServiceAccount(
    @SerialName("private_key_id") val privateKeyId: String,
    @SerialName("client_x509_cert_url") val clientX509CertificatesUrl: String,
)

@Serializable
private data class FirebaseCustomTokenClaims(
    @SerialName("user_id") val userId: String,
    val email: String,
    @SerialName("email_verified") val emailVerified: Boolean,
)

private val httpClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

private fun readServiceAccountFile(): ServiceAccount {
    val serviceAccountFilePath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        ?: "../../infra/gcp/secrets/gcp-service-account.json"
    val gcpServiceAccountFileJson = File(serviceAccountFilePath).readText()
    return jsonParser.decodeFromString(gcpServiceAccountFileJson)
}

suspend fun getClientX509Certificate(): X509Certificate {
    val serviceAccount = readServiceAccountFile()
    val clientCertMap = httpClient.get(serviceAccount.clientX509CertificatesUrl).body<Map<String, String>>()
    val clientPublicKey = clientCertMap[serviceAccount.privateKeyId]
    return X509CertUtils.parse(clientPublicKey) ?: throw Exception("Failed to parse client X509 cert")
}

suspend fun verifyFirebaseCustomToken(
    uid: String,
    email: String,
    firebaseCustomToken: String
) {
    val claimsJson = jacksonObjectMapper.writeValueAsString(JWTParser.parse(firebaseCustomToken).jwtClaimsSet.claims["claims"])
    val firebaseCustomTokenClaims: FirebaseCustomTokenClaims = jsonParser.decodeFromString(claimsJson)
    firebaseCustomTokenClaims.userId shouldBe uid
    firebaseCustomTokenClaims.email shouldBe email
    firebaseCustomTokenClaims.emailVerified shouldBe true

    val x509 = getClientX509Certificate()
    val rsaKey = RSAKey.parse(x509)
    val verifier = RSASSAVerifier(rsaKey)

    val signedJWT = SignedJWT.parse(firebaseCustomToken)

    signedJWT.verify(verifier)
}
