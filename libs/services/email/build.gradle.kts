plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(project("email-api"))
    implementation(project("sendgrid"))

    // test
    testImplementation("io.kotest:kotest-runner-junit5-jvm:_")
}