plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {

    implementation(project(":apps:oauth2-provider-emulator:oauth2-provider-api"))

    implementation(project(":libs:utils:logging"))

    implementation(Ktor.server.core)
    implementation(Ktor.features.auth)
    implementation(Ktor.features.serialization)
    runtimeOnly(Ktor.server.netty)

    implementation("com.nimbusds:nimbus-jose-jwt:_")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    applicationName = "oauth2-provider-emulator"
    applicationDefaultJvmArgs = listOf("-Dlogback.configurationFile=logback.xml")
}