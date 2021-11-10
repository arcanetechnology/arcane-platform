plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation("io.ktor:ktor-server-core:${Version.ktor}")
}