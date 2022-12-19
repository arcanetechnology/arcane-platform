plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":libs:services:email:email-api"))
    implementation("com.sendgrid:sendgrid-java:_")
    implementation(project(":libs:utils:config"))
    implementation(project(":libs:utils:logging"))

    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:_")

    implementation(KotlinX.coroutines.core)
    implementation(KotlinX.coroutines.jdk8)

    implementation(KotlinX.serialization.core)
    implementation(KotlinX.serialization.json)
}