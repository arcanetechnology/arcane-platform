plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(project(":libs:utils:logging"))
    implementation("org.slf4j:slf4j-api:_")
    implementation("io.micronaut.gcp:micronaut-gcp-logging:_")
    implementation("ch.qos.logback:logback-classic:_")
    implementation("ch.qos.logback.contrib:logback-json-classic:_")

    // test
    testImplementation("io.kotest:kotest-runner-junit5-jvm:_")
}