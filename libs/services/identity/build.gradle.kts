plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(project("identity-api"))
    implementation(project("gcp"))
    implementation(project("apple"))

    implementation(Ktor.server.core)
    implementation("io.ktor:ktor-server-auth:_")
}