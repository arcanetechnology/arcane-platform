plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation("io.arrow-kt:arrow-core:_")

    // test
    testImplementation("io.kotest:kotest-runner-junit5-jvm:_")
}