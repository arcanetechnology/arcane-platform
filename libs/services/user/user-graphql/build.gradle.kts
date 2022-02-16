plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":libs:utils:graphql"))
    implementation(project(":libs:services:user:user-service"))
}