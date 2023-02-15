plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {

    implementation(project(":libs:services:identity"))
    implementation(project(":libs:utils:logging"))
    implementation(project(":libs:services:payment:stripe"))

    implementation(Ktor.server.core)
    implementation("io.ktor:ktor-server-auth:_")
}