package no.arcane.platform.identity.auth.apple

import java.security.PrivateKey

data class Config(
    val teamId: String,
    val keyId: String,
    val clientId: String,
    val privateKey: PrivateKey,
    val appleIdServiceUrl:String = "https://appleid.apple.com",
)

data class FileConfig(
    val teamId: String,
    val keyId: String,
    val clientId: String,
    val privateKey: String,
)