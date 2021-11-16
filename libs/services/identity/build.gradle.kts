plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(project("identity-api"))
    implementation(project("gcp"))
    implementation(project("apple"))

    implementation("io.ktor:ktor-server-core:${Version.ktor}")
    implementation("io.ktor:ktor-auth:${Version.ktor}")
}