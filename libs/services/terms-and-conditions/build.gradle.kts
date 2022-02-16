plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":libs:services:terms-and-conditions:terms-and-conditions-endpoint"))
    implementation(project(":libs:services:terms-and-conditions:terms-and-conditions-graphql"))
}