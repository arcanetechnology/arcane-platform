plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":libs::utils:logging"))
    implementation(Ktor.client.core)
    implementation(Ktor.client.cio)
    implementation(Ktor.client.logging)
    implementation(Ktor.client.serialization)
    implementation("io.ktor:ktor-client-content-negotiation:_")
    implementation("io.ktor:ktor-serialization-kotlinx-json:_")
}