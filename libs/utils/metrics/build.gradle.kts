plugins {
    `java-library`
    kotlin("jvm")
}

repositories {
    maven("https://repo.spring.io/milestone")
}
dependencies {
    api(project(":libs:utils:logging"))
    api(project(":libs:utils:config"))
    api("io.micrometer:micrometer-registry-stackdriver:_")

    // test
    testImplementation("io.kotest:kotest-runner-junit5-jvm:_")
}

tasks.withType<Test> {
    useJUnitPlatform()
}