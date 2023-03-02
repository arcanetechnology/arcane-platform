plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":libs:services:user:user-model"))
    implementation(project(":libs:services:email"))
    implementation(project(":libs:utils:config"))
}