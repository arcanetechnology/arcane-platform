package no.arcane.platform.tests

val backend = Socket(
    host = System.getenv("BACKEND_HOST") ?: "localhost",
    port = 8080,
)

val usingEsp = backend.host != "arcane-platform-app"

val viaIde = backend.host == "localhost"

data class Socket(
    val host: String,
    val port: Int
)

val oauth2ProviderEmulator = Socket(
    host = if (viaIde) "" else "oauth2-provider-emulator",
    port = if (viaIde) 8081 else 8080,
)
