package no.arcane.platform.identity.auth.gcp

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.util.*

private const val AUTH_CONFIG_NAME = "esp-v2-header"

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

fun Authentication.Configuration.gcpEndpointsAuthConfig() {
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

private val jsonSerializer = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

private fun ApplicationRequest.espV2Header(): EspV2Header? {
    val userInfo = header(GcpHttpHeaders.UserInfo) ?: return null
    val userInfoJson = String(Base64.getDecoder().decode(userInfo))
    return try {
        jsonSerializer.decodeFromString<EspV2Header>(userInfoJson)
    } catch (e: Exception) {
        null
    }
}

fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
        mdc("userId") {
            it.request.espV2Header()?.userId
        }
        // https://cloud.google.com/logging/docs/structured-logging#special-payload-fields
        callIdMdc("logging.googleapis.com/trace")
    }
    routing {
        authenticate(AUTH_CONFIG_NAME) {
            get("/whoami") {
                val userInfo = call.request.headers[GcpHttpHeaders.UserInfo]
                    ?.let { userInfo -> String(Base64.getDecoder().decode(userInfo)) }
                    ?: ""
                val jsonElement = jsonSerializer.parseToJsonElement(userInfo)
                call.respondText(jsonSerializer.encodeToString(jsonElement), contentType = ContentType.Application.Json)
            }
        }
    }
}