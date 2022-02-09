plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":libs:services:identity:identity-api"))

    implementation(Ktor.server.core)
    implementation(Ktor.features.auth)

    implementation(KotlinX.serialization.json)
}