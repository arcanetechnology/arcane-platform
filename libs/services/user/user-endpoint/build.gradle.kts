plugins {
    kotlin("jvm")
}

dependencies {

    implementation(project(":libs:services:identity"))
    implementation(project(":libs:utils:logging"))

    implementation(Ktor.server.core)
    implementation("io.ktor:ktor-server-auth:_")

    implementation(project(":libs:services:user:user-service"))
}