package no.arcane.platform.identity.auth.apple

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.arcane.platform.identity.auth.gcp.FirebaseAuthService
import no.arcane.platform.utils.logging.logWithMDC
import java.util.*

private const val APPLE_OAUTH2 = "apple-oauth2"

object GcpHttpHeaders {
    const val UserInfo = "X-Endpoint-API-UserInfo"
}

class GcpEndpointsAuthProvider internal constructor(
    configuration: Config
) : AuthenticationProvider(configuration) {

    /**
     * GCP Endpoints Header Auth configuration
     */
    class Configuration internal constructor(name: String?) : Config(name)

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val espV2Header = context.call.request.espV2Header()
        if (espV2Header == null) {
            context.challenge(
                OAuthKey,
                AuthenticationFailedCause.NoCredentials,
            ) { challenge, call ->
                call.respond(UnauthorizedResponse())
                challenge.complete()
            }
            return
        }
        context.principal(
            UserIdPrincipal(espV2Header.email)
        )
    }
}

fun AuthenticationConfig.appleJwtAuthConfig() {
    register(GcpEndpointsAuthProvider(GcpEndpointsAuthProvider.Configuration(APPLE_OAUTH2)))
}

@Serializable
data class EspV2Header(
    val email: String,
) : Credential

fun ApplicationRequest.espV2Header(): EspV2Header? {
    val userInfo = header(GcpHttpHeaders.UserInfo) ?: return null
    val userInfoJson = String(Base64.getUrlDecoder().decode(userInfo))
    val jsonFormat = Json {
        ignoreUnknownKeys = true
    }
    return jsonFormat.decodeFromString<EspV2Header>(userInfoJson)
}

fun Application.module() {

    routing {
        authenticate(APPLE_OAUTH2) {
            get("/firebase-custom-token") {
                val email = call.principal<UserIdPrincipal>()!!.name
                val uid = FirebaseAuthService.createOrMergeUser(
                    email = email,
                    displayName = "",
                )
                logWithMDC("userId" to uid) {
                    val firebaseCustomToken = FirebaseAuthService.createCustomToken(uid = uid, email = email)
                    call.respondText(firebaseCustomToken)
                }
            }
        }
    }
}