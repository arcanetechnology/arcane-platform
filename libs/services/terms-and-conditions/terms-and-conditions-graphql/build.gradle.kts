plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":libs:utils:graphql"))
    implementation(project(":libs:services:terms-and-conditions:terms-and-conditions-service"))
}