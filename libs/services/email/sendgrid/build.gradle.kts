plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation(project(":libs:services:email:email-api"))
    implementation("com.sendgrid:sendgrid-java:4.0.1")
    implementation(project(":libs:utils:config"))
    implementation(project(":libs:utils:logging"))
}