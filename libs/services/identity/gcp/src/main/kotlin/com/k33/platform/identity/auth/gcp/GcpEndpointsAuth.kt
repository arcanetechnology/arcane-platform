package com.k33.platform.identity.auth.gcp

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.util.*

private const val ESP_V2_HEADER = "esp-v2-header"

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
        when (val espV2Header = context.call.request.espV2Header()) {
            null -> context.challenge(
                OAuthKey,
                AuthenticationFailedCause.NoCredentials,
            ) { challenge, call ->
                call.respond(UnauthorizedResponse())
                challenge.complete()
            }
            else -> context.principal(
                UserInfo(
                    userId = espV2Header.userId,
                    email = espV2Header.email,
                )
            )
        }
    }
}

fun AuthenticationConfig.gcpEndpointsAuthConfig() {
    register(GcpEndpointsAuthProvider(GcpEndpointsAuthProvider.Configuration(ESP_V2_HEADER)))
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
    val userInfoJson = String(Base64.getUrlDecoder().decode(userInfo))
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
        authenticate(ESP_V2_HEADER) {
            get("/whoami") {
                val userInfo = call.request.headers[GcpHttpHeaders.UserInfo]
                    ?.let { userInfo -> String(Base64.getUrlDecoder().decode(userInfo)) }
                    ?: ""
                val jsonElement = jsonSerializer.parseToJsonElement(userInfo)
                call.respondText(jsonSerializer.encodeToString(jsonElement), contentType = ContentType.Application.Json)
            }
        }
    }
}