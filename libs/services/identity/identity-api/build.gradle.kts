plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation("io.ktor:ktor-auth:${Version.ktor}")
}