plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api("org.slf4j:slf4j-api:${Version.slf4j}")
    runtimeOnly("ch.qos.logback:logback-classic:${Version.logback}")
}