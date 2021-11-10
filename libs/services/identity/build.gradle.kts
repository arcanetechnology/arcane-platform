plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(project("identity-api"))
    implementation(project("gcp"))
}