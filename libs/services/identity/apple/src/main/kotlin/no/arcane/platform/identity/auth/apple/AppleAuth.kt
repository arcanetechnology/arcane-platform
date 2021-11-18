package no.arcane.platform.identity.auth.apple

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.arcane.platform.identity.auth.gcp.FirebaseAuthService
import org.slf4j.LoggerFactory
import java.util.*

private const val AUTH_CONFIG_NAME = "apple-oauth2"

// private val logger by lazy { LoggerFactory.getLogger("no.arcane.platform.identity.auth.apple.AppleAuth") }

object GcpHttpHeaders {
    const val UserInfo = "X-Endpoint-API-UserInfo"
}

class GcpEndpointsAuthProvider internal constructor(
    configuration: Configuration
) : AuthenticationProvider(configuration) {

    /**
     * GCP Endpoints Header Auth configuration
     */
    class Configuration internal constructor(name: String?) : AuthenticationProvider.Configuration(name)
}

fun Authentication.Configuration.appleJwtAuthConfig() {
    val provider = GcpEndpointsAuthProvider(GcpEndpointsAuthProvider.Configuration(AUTH_CONFIG_NAME))

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        when (val espV2Header = call.request.espV2Header()) {
            null -> context.challenge(
                OAuthKey,
                AuthenticationFailedCause.NoCredentials,
            ) {
                call.respond(UnauthorizedResponse())
                it.complete()
            }
            else -> context.principal(
                UserIdPrincipal(espV2Header.email)
            )
        }
    }

    register(provider)
}

@Serializable
data class EspV2Header(
    val email: String,
) : Credential

fun ApplicationRequest.espV2Header(): EspV2Header? {
    val userInfo = header(GcpHttpHeaders.UserInfo) ?: return null
    val userInfoJson = String(Base64.getDecoder().decode(userInfo))
    val jsonFormat = Json {
        ignoreUnknownKeys = true
    }
    return jsonFormat.decodeFromString<EspV2Header>(userInfoJson)
}

fun Application.module() {

    routing {
        authenticate(AUTH_CONFIG_NAME) {
            get("/firebase-custom-token") {
                val email = call.principal<UserIdPrincipal>()!!.name
                val uid = FirebaseAuthService.createOrMergeUser(
                    email = email,
                    displayName = "",
                )
                val firebaseCustomToken = FirebaseAuthService.createCustomToken(uid = uid, email = email)
                call.respondText(firebaseCustomToken)
            }
        }
    }
}

/*
fun Authentication.Configuration.appleJwtAuthConfig() {
    val issuer = "https://appleid.apple.com"
    val audiences = arrayOf("no.arcane.platform.client.web")
    val issuerPublicKeyJwkUrl = URL("https://appleid.apple.com/auth/keys")

    val jwkProvider = JwkProviderBuilder(issuerPublicKeyJwkUrl)
        .cached(false)
        .build()

    jwt(AUTH_CONFIG_NAME) {
        verifier(jwkProvider, issuer) { // no need of withIssuer() when it is passed here
            withAnyOfAudience(*audiences)
            withClaimPresence("email")
            withClaim("email_verified", "true")
        }
        validate { credential ->
            if (credential.payload.getClaim("email").asString().isNullOrBlank()) {
                null
            } else {
                JWTPrincipal(credential.payload)
            }
        }
    }
}
*/