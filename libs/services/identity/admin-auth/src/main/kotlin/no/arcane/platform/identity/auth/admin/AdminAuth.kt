package no.arcane.platform.identity.auth.admin

import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.arcane.platform.identity.auth.gcp.AdminApp
import no.arcane.platform.identity.auth.gcp.AdminInfo
import java.util.*

object GcpHttpHeaders {
    const val UserInfo = "X-Endpoint-API-UserInfo"
}

class SamlAuthProvider internal constructor(
    private val config: Config
) : AuthenticationProvider(config) {

    /**
     * GCP Endpoints Header Auth configuration
     */
    class Config internal constructor(
        name: String,
        val adminApps: List<AdminApp>
    ) : AuthenticationProvider.Config(name)

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        fun returnUnauthorizedResponse() {
            context.challenge(
                OAuthKey,
                AuthenticationFailedCause.NoCredentials,
            ) { challenge, call ->
                call.respond(UnauthorizedResponse())
                challenge.complete()
            }
        }

        val espV2SamlHeader = context.call.request.espV2SamlHeader()
        if (espV2SamlHeader == null) {
            returnUnauthorizedResponse()
            return
        }
        val firebase = espV2SamlHeader.firebase
        if (firebase.signInProvider != "saml.google.arcane.no") {
            returnUnauthorizedResponse()
            return
        }
        val adminApp = config.adminApps.find { adminApp ->
            firebase.tenant.startsWith(
                prefix = "${adminApp.name.lowercase()}-admin-",
                ignoreCase = true,
            )
        }
        if (adminApp == null) {
            returnUnauthorizedResponse()
            return
        }
        context.principal(
            AdminInfo(
                email = espV2SamlHeader.email,
                adminApp = adminApp,
            )
        )
    }
}

fun AuthenticationConfig.adminAuthConfig(
    name: String,
    adminApps: List<AdminApp>,
) {
    val config = SamlAuthProvider.Config(name, adminApps)
    val provider = SamlAuthProvider(config)
    register(provider)
}

@Serializable
data class EspV2SamlHeader(
    val email: String,
    val firebase: Firebase,
)

@Serializable
data class Firebase(
    val tenant: String,
    @SerialName("sign_in_provider") val signInProvider: String,
)

private val jsonSerializer = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

private fun ApplicationRequest.espV2SamlHeader(): EspV2SamlHeader? {
    val userInfo = header(GcpHttpHeaders.UserInfo) ?: return null
    val userInfoJson = String(Base64.getUrlDecoder().decode(userInfo))
    return try {
        jsonSerializer.decodeFromString<EspV2SamlHeader>(userInfoJson)
    } catch (e: Exception) {
        null
    }
}