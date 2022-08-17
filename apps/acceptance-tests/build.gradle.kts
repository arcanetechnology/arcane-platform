plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":libs:utils:logging"))
    implementation(project(":apps:oauth2-provider-emulator:oauth2-provider-api"))
    implementation(project(":libs:utils:firebase-auth"))
    implementation("com.nimbusds:nimbus-jose-jwt:_")
    implementation(Ktor.client.cio)
    implementation(Ktor.client.logging)
    implementation(Ktor.client.serialization)
    implementation("io.ktor:ktor-client-content-negotiation:_")
    implementation("io.ktor:ktor-serialization-kotlinx-json:_")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:_")
    implementation("io.kotest:kotest-runner-junit5-jvm:_")
    implementation("org.junit.platform:junit-platform-console:_")
}

application {
    mainClass.set("org.junit.platform.console.ConsoleLauncher")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
}