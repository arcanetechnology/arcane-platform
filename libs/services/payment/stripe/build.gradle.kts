plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":libs:services:user:user-model"))
    implementation(project(":libs:utils:logging"))
    implementation("com.stripe:stripe-java:_")
}