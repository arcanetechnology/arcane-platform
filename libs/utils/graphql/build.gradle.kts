plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {

    implementation(project(":libs:services:identity"))
    implementation(project(":libs:utils:logging"))

    api("com.graphql-java:graphql-java:_")

    api(Ktor.server.core)
    api(KotlinX.coroutines.core)
    api(KotlinX.coroutines.jdk8)
    implementation(Ktor.features.auth)
    implementation(KotlinX.serialization.json)

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:_")

    // test
    testImplementation("io.kotest:kotest-runner-junit5-jvm:_")
}