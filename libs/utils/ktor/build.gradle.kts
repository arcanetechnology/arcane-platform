plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api("io.ktor:ktor-server-core:${Version.ktor}")
    api("io.ktor:ktor-server-netty:${Version.ktor}")
}