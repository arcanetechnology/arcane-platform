plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib")
}

dependencies {

    implementation(project(":apps:oauth2-provider-emulator:oauth2-provider-api"))

    implementation(project(":libs:utils:logging"))

    implementation("io.ktor:ktor-server-core:${Version.ktor}")
    implementation("io.ktor:ktor-auth:${Version.ktor}")
    implementation("io.ktor:ktor-serialization:${Version.ktor}")
    runtimeOnly("io.ktor:ktor-server-netty:${Version.ktor}")

    implementation("com.nimbusds:nimbus-jose-jwt:${Version.nimbusJoseJwt}")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    applicationName = "oauth2-provider-emulator"
    applicationDefaultJvmArgs = listOf("-Dlogback.configurationFile=logback.xml")
}

jib {
    from.image = "azul/zulu-openjdk:17.0.2-17.32.13"
    to.image = "oauth2-provider-emulator"
    container {
        mainClass = application.mainClass.get()
    }
}