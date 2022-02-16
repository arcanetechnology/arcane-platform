plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {

    implementation(project(":libs:services:identity"))
    implementation(project(":libs:utils:logging"))

    implementation(Ktor.server.core)
    implementation(Ktor.features.auth)

    implementation(project(":libs:services:terms-and-conditions:terms-and-conditions-service"))
}