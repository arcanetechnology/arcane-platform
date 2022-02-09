plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation(Ktor.features.auth)
}