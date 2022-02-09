plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation("com.google.firebase:firebase-admin:_")
    implementation(KotlinX.coroutines.core)
    implementation(project(":libs:utils:logging"))
}