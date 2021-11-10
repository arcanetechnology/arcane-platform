package no.arcane.platform.identity.auth

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.*

fun Application.module() {
    install(Authentication) {
        gcpEndpointsAuth("esp-v2-header")
    }
    routing {
        authenticate("esp-v2-header") {
            get("/whoami") {
                val userInfo = call.request.headers[GcpHttpHeaders.UserInfo]
                    ?.let { userInfo -> String(Base64.getDecoder().decode(userInfo)) }
                    ?: ""
                val jsonFormat = Json {
                    prettyPrint = true
                }
                val jsonElement = jsonFormat.parseToJsonElement(userInfo)
                call.respondText(jsonFormat.encodeToString(jsonElement), contentType = ContentType.Application.Json)
            }
        }
    }
}

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

fun Authentication.Configuration.gcpEndpointsAuth(
    name: String? = null,
) {
    val provider = GcpEndpointsAuthProvider(GcpEndpointsAuthProvider.Configuration(name))

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
                UserInfo(
                    userId = espV2Header.userId,
                    email = espV2Header.email,
                )
            )
        }
    }

    register(provider)
}

@Serializable
data class EspV2Header(
    @SerialName("user_id") val userId: String,
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