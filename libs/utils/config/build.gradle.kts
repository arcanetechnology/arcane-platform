plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {

    api("io.github.config4k:config4k:${Version.config4k}")

    implementation(project(":libs:utils:logging"))

    // test
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${Version.kotest}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}