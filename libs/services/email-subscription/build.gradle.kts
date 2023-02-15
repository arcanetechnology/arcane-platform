plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":libs:services:email"))
    implementation(project(":libs:services:email:sendgrid"))

    implementation(Ktor.server.core)
    implementation("io.ktor:ktor-serialization-kotlinx-json:_")
}