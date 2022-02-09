plugins {
    kotlin("jvm")
}

dependencies {

    implementation(project(":libs:services:identity"))
    implementation(project(":libs:utils:logging"))

    implementation(Ktor.server.core)
    implementation(Ktor.features.auth)

    implementation(project(":libs:services:user:model"))
}