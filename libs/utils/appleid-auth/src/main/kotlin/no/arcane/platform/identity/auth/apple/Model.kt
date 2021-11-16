package no.arcane.platform.identity.auth.apple

enum class GrantType {
    authorization_code,
    refresh_token,
}

/**
 * https://developer.apple.com/documentation/sign_in_with_apple/tokenresponse
 */
data class TokenResponse(
    val access_token: String,
    val expires_in: Long,
    val id_token: String,
    val refresh_token: String,
    val token_type: String,
)

/**
 * https://developer.apple.com/documentation/sign_in_with_apple/errorresponse
 */
data class ErrorResponse(
    val error: Error,
    val error_description: String,
)

/**
 * https://developer.apple.com/documentation/sign_in_with_apple/errorresponse
 */
enum class Error(val cause: String) {
    invalid_request("The request is malformed."),
    invalid_client("The client authentication failed."),
    invalid_grant("The authorization grant or refresh token is invalid."),
    unauthorized_client("The client is not authorized to use this authorization grant type."),
    unsupported_grant_type("The authenticated client is not authorized to use the grant type."),
    invalid_scope("The requested scope is invalid."),

    UNEXPECTED(""),
}

/**
 * https://developer.apple.com/documentation/sign_in_with_apple/jwkset
 */
data class JWKSet(val keys: Collection<JWKKey>)

/**
 * https://developer.apple.com/documentation/sign_in_with_apple/jwkset/keys
 */
data class JWKKey(
    val alg: String,
    val e: String,
    val kid: String,
    val kty: String,
    val n: String,
    val use: String,
)