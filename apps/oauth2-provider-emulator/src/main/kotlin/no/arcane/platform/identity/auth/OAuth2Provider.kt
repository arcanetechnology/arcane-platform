package no.arcane.platform.identity.auth

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import java.util.*


fun Application.module() {

    install(ContentNegotiation) {
        json()
    }

    val rsaKey = RSAKeyGenerator(2048)
        .keyUse(KeyUse.SIGNATURE)
        .keyID(UUID.randomUUID().toString())
        .generate()

    val signer = RSASSASigner(rsaKey)

    val jwtHeader = JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.keyID).build()

    routing {

        get ("auth/keys") {
            call.respondText("""{ "keys": [ ${rsaKey.toPublicJWK()} ] }""", ContentType.Application.Json)
        }

        get("firebase-id-token") {
            val firebaseIdTokenPayload = call.receive<FirebaseIdTokenPayload>()

            val claims = JWTClaimsSet.Builder()
                .claim("name", firebaseIdTokenPayload.name)
                .claim("picture", firebaseIdTokenPayload.picture)
                .issuer(firebaseIdTokenPayload.issuer)
                .audience(firebaseIdTokenPayload.audience)
                .claim("user_id", firebaseIdTokenPayload.subject)
                .subject(firebaseIdTokenPayload.subject)
                .claim("email", firebaseIdTokenPayload.email)
                .claim("email_verified", firebaseIdTokenPayload.emailVerified)
                .build()

            val jwt = SignedJWT(jwtHeader, claims)
            jwt.sign(signer)

            call.respondText(jwt.serialize())
        }

        get("apple-id-token") {
            val appleIdTokenPayload = call.receive<AppleIdTokenPayload>()

            val claims = JWTClaimsSet.Builder()
                .issuer(appleIdTokenPayload.issuer)
                .subject(appleIdTokenPayload.subject)
                .audience(appleIdTokenPayload.audience)
                .claim("name", appleIdTokenPayload.name)
                .claim("email", appleIdTokenPayload.email)
                .claim("email_verified", appleIdTokenPayload.emailVerified)
                .claim("is_private_email", appleIdTokenPayload.isPrivateEmail)
                .claim("real_user_status", appleIdTokenPayload.realUserStatus)
                .build()

            val jwt = SignedJWT(jwtHeader, claims)
            jwt.sign(signer)

            call.respondText(jwt.serialize())
        }
    }
}