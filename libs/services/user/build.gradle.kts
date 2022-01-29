plugins {
    kotlin("jvm")
}

dependencies {

    implementation(project(":libs:services:identity"))
    implementation(project(":libs:utils:logging"))

    implementation("io.ktor:ktor-server-core:${Version.ktor}")
    implementation("io.ktor:ktor-auth:${Version.ktor}")

    implementation(project(":libs:services:user:model"))
}