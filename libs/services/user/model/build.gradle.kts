plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {

    api("dev.vihang.firestore4k:typed-api:_")
    // TODO should be available via firestore4k
    api(KotlinX.serialization.properties)
}
