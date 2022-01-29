plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {

    implementation(project(":libs:services:identity"))
    implementation(project(":libs:services:email"))
    implementation(project(":libs:utils:cms"))
    implementation(project(":libs:utils:logging"))

    implementation("io.ktor:ktor-server-core:${Version.ktor}")
    implementation("io.ktor:ktor-auth:${Version.ktor}")

    implementation(project(":libs:services:user:model"))

    implementation("dev.vihang.firestore4k:typed-api:${Version.firestore4k}")
    // TODO should be available via firestore4k
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:${Version.kotlinSerialization}")
}