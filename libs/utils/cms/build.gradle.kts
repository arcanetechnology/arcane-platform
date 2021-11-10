plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(project("cms-api"))
    implementation(project("contentful"))
}