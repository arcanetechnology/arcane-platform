plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.google.firebase:firebase-admin:_")

    implementation(project(":libs:utils:google-coroutine-ktx"))
    implementation(project(":libs:utils:file-store"))

    implementation(KotlinX.coroutines.core)
    implementation(Ktor.server.core)

    implementation(project(":libs:utils:logging"))
}