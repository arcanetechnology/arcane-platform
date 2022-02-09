plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api("org.slf4j:slf4j-api:_")
    runtimeOnly("ch.qos.logback:logback-classic:_")
}