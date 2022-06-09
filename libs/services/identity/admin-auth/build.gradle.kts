plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":libs:services:identity:identity-api"))

    implementation(Ktor.server.core)
    implementation("io.ktor:ktor-server-auth:_")
    implementation("io.ktor:ktor-server-call-logging:_")
    implementation("io.ktor:ktor-server-call-id-jvm:_")

    implementation(KotlinX.serialization.json)
}