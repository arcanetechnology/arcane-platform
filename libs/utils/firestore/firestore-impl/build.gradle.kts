plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation("com.google.cloud:google-cloud-firestore")
    implementation(platform("com.google.cloud:libraries-bom:${Version.googleCloudBom}"))
}