plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {

    implementation(project(":apps:oauth2-provider-emulator:oauth2-provider-api"))

    implementation(project(":libs:utils:logging"))

    implementation(Ktor.server.core)
    implementation("io.ktor:ktor-server-auth:_")
    implementation("io.ktor:ktor-server-content-negotiation:_")
    implementation("io.ktor:ktor-serialization-kotlinx-json:_")
    runtimeOnly(Ktor.server.netty)

    implementation("com.nimbusds:nimbus-jose-jwt:_")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    applicationName = "oauth2-provider-emulator"
    applicationDefaultJvmArgs = listOf("-Dlogback.configurationFile=logback.xml")
}