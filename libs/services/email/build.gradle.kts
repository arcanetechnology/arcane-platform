plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(project("email-api"))
    implementation(project("sendgrid"))
}