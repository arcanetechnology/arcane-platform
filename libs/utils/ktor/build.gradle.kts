plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation(Ktor.server.core)
    implementation("io.ktor:ktor-server-call-id:_")
    implementation("io.ktor:ktor-server-content-negotiation:_")
    implementation("io.ktor:ktor-server-default-headers:_")
    implementation("io.ktor:ktor-serialization-kotlinx-json:_")
    implementation("io.ktor:ktor-server-status-pages:_")
}