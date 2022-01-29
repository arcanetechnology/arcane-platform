plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {

    api("dev.vihang.firestore4k:typed-api:${Version.firestore4k}")
    // TODO should be available via firestore4k
    api("org.jetbrains.kotlinx:kotlinx-serialization-properties:${Version.kotlinSerialization}")
}
