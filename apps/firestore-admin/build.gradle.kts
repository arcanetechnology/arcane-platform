plugins {
    application
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":libs:utils:firebase-auth"))
    implementation(project(":libs:services:user:user-model"))
    implementation(project(":libs:apps:invest"))
}