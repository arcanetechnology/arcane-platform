package no.arcane.platform.identity.auth.apple

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.getLogger
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.*

object AppleIdAuthClient {

    private val logger by getLogger()

    private val config by lazy {
        val fileConfig by loadConfig<FileConfig>("appleid-auth", "appleid-auth")
        Config(
            teamId = fileConfig.teamId,
            keyId = fileConfig.keyId,
            clientId = fileConfig.clientId,
            privateKey = KeyFactory
                .getInstance("EC")
                .generatePrivate(
                    PKCS8EncodedKeySpec(
                        Base64.getDecoder().decode(fileConfig.privateKey)
                    )
                ),
        )
    }

    private val client by lazy {
        HttpClient {
            defaultRequest {
                host = "appleid.apple.com"
                url {
                    this.protocol = URLProtocol.HTTPS
                }
                expectSuccess = false
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(UserAgent) {
                agent = "arcane-platform"
            }
            install(JsonFeature) {
                serializer = JacksonSerializer()
            }
        }
    }

    suspend fun fetchApplePublicKey(): Either<String, JWKSet> {
        val response: HttpResponse = client.get(path = "auth/keys")
        return when (response.status.value) {
            200 -> response.receive<JWKSet>().right()
            else -> response.readText().left()
        }
    }

    /**
     * https://developer.apple.com/documentation/sign_in_with_apple/generate_and_validate_tokens
     */
    suspend fun authorize(
        authCode: String,
        redirectUri: String,
    ): Either<ErrorResponse, TokenResponse> {
        val response: HttpResponse = client.post(
            path = "auth/token",
            body = FormDataContent(
                Parameters.build {
                    append("client_id", config.clientId)
                    append("client_secret", generateClientSecret())
                    append("code", authCode)
                    append("grant_type", GrantType.authorization_code.name)
                    append("redirect_uri", redirectUri)
                }
            ),
        )
        return when (response.status.value) {
            200 -> response.receive<TokenResponse>().right()
            400 -> response.receive<ErrorResponse>().left()
            else -> {
                logger.warn("Unexpected response ${response.status.value} - ${response.readText()}")
                ErrorResponse(Error.UNEXPECTED, response.readText()).left()
            }
        }
    }

    /**
     * https://developer.apple.com/documentation/sign_in_with_apple/generate_and_validate_tokens
     */
    suspend fun validate(token: String): Either<ErrorResponse, TokenResponse> {
        val response: HttpResponse = client.post(
            path = "auth/token",
            body = FormDataContent(
                Parameters.build {
                    append("client_id", config.clientId)
                    append("client_secret", generateClientSecret())
                    append("grant_type", GrantType.refresh_token.name)
                    append("refresh_token", token)
                }
            ),
        )
        return when (response.status.value) {
            200 -> response.receive<TokenResponse>().right()
            400 -> response.receive<ErrorResponse>().left()
            else -> {
                logger.warn("Unexpected response ${response.status.value} - ${response.readText()}")
                ErrorResponse(Error.UNEXPECTED, response.readText()).left()
            }
        }
    }

    private fun generateClientSecret(): String {
        val now = Instant.now()
        return Jwts.builder()
            .setHeader("kid" to config.keyId)
            .setIssuer(config.teamId)
            .setIssuedAt(Date(now.toEpochMilli()))
            .setExpiration(Date(now.plusSeconds(300).toEpochMilli()))
            .setAudience(config.appleIdServiceUrl)
            .setSubject(config.clientId)
            .signWith(config.privateKey, SignatureAlgorithm.ES256)
            .compact()
    }

    private fun JwtBuilder.setHeader(header: Pair<String, String>): JwtBuilder {
        this.setHeader(mapOf(header))
        return this
    }
}
