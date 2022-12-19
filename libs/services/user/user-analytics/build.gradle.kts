plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation("com.google.firebase:firebase-admin:_")

    implementation(project(":libs:utils:google-coroutine-ktx"))
    implementation(project(":libs:utils:file-store"))

    implementation(KotlinX.coroutines.core)
    implementation(Ktor.server.core)

    implementation(KotlinX.serialization.json)

    implementation(project(":libs:services:email"))
    implementation(project(":libs:services:email:sendgrid"))

    implementation(project(":libs:utils:logging"))
}