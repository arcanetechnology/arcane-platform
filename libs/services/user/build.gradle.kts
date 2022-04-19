plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":libs:services:user:user-endpoint"))
    implementation(project(":libs:services:user:user-graphql"))
    implementation(project(":libs:services:user:user-analytics"))
}