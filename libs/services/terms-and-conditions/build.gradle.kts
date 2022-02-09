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

    implementation(Ktor.server.core)
    implementation(Ktor.features.auth)

    implementation(project(":libs:services:user:model"))

    implementation("dev.vihang.firestore4k:typed-api:_")
    // TODO should be available via firestore4k
    implementation(KotlinX.serialization.properties)
}