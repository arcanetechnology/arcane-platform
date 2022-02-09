plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {

    api("io.github.config4k:config4k:_")

    implementation(project(":libs:utils:logging"))

    // test
    testImplementation("io.kotest:kotest-runner-junit5-jvm:_")
}

tasks.withType<Test> {
    useJUnitPlatform()
}