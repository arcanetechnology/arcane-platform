plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation("com.google.firebase:firebase-admin:${Version.firebase}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.kotlinCoroutines}")
    implementation(project(":libs:utils:logging"))
}