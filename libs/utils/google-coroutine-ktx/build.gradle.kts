plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation("com.google.api:api-common:_")
    implementation("com.google.guava:guava:_")
    implementation(KotlinX.coroutines.core)
}