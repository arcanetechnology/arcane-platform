plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {

    implementation(project(":libs:services:email"))
    implementation(project(":libs:utils:cms"))
    implementation(project(":libs:utils:logging"))

    api(project(":libs:services:user:user-model"))

    implementation("io.firestore4k:typed-api:_")
}