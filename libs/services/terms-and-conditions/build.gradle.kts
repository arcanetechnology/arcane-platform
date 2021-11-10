plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {

    implementation(project(":libs:services:identity"))
    implementation(project(":libs:services:email"))
    implementation(project(":libs:utils:cms"))
    implementation(project(":libs:utils:logging"))

    implementation("io.ktor:ktor-server-core:${Version.ktor}")
    implementation("io.ktor:ktor-auth:${Version.ktor}")
}