plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation("io.ktor:ktor-server-core:${Version.ktor}")
    implementation("io.ktor:ktor-serialization:${Version.ktor}")
}