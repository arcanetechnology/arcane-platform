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
import kotlinx.serialization.Serializable
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

        get("id-token") {
            val requestedClaims = call.receive<Claims>()

            val claims = JWTClaimsSet.Builder()
                .claim("name", requestedClaims.name)
                .claim("picture", requestedClaims.picture)
                .issuer("oauth2-provider-emulator")
                .audience("acceptance-tests")
                .claim("user_id", requestedClaims.subject)
                .subject(requestedClaims.subject)
                .claim("email", requestedClaims.email)
                .claim("email_verified", true)
                .build()

            val jwt = SignedJWT(jwtHeader, claims)
            jwt.sign(signer)

            call.respondText(jwt.serialize())
        }
    }
}

@Serializable
data class Claims(
    val name: String,
    val picture: String,
    val subject: String,
    val email: String,
)