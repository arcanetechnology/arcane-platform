plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":libs:utils:config"))
    implementation(project(":libs:utils:logging"))
    implementation(project(":libs:utils:arrow-ktx"))

    implementation(Ktor.server.core)
    implementation("io.ktor:ktor-server-auth:_")

    implementation(project(":libs:services:user:user-service"))

    implementation("com.google.firebase:firebase-admin:_")
    implementation(project(":libs:utils:google-coroutine-ktx"))

    implementation("io.arrow-kt:arrow-core:_")

    implementation(KotlinX.serialization.core)
    implementation(KotlinX.serialization.json)
}