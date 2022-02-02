plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(project(":libs:utils:logging"))
    implementation("org.slf4j:slf4j-api:${Version.slf4j}")
    implementation("io.micronaut.gcp:micronaut-gcp-logging:${Version.micronaut}")
    implementation("ch.qos.logback:logback-classic:${Version.logback}")
    implementation("ch.qos.logback.contrib:logback-json-classic:${Version.logbackJsonClassic}")

    // test
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${Version.kotest}")
}