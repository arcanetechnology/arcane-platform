plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation(Ktor.server.core)
    implementation(Ktor.features.serialization)
}