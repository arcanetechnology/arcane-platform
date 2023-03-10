plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":libs:services:user:user-model"))
    implementation(project(":libs:services:email"))
    implementation(project(":libs:utils:logging"))
    implementation(project(":libs:utils:config"))
    implementation("com.stripe:stripe-java:_")
    implementation("com.google.code.gson:gson:_")

    implementation(Ktor.server.core)
}