plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":libs:utils:logging"))
    implementation(project(":apps:oauth2-provider-emulator:oauth2-provider-api"))
    implementation(Ktor.client.cio)
    implementation(Ktor.client.logging)
    implementation(Ktor.client.serialization)
    implementation("io.kotest:kotest-runner-junit5-jvm:_")
    implementation("org.junit.platform:junit-platform-console:_")
}

application {
    mainClass.set("org.junit.platform.console.ConsoleLauncher")
}

jib {
    from.image = "azul/zulu-openjdk:17.0.2-17.32.13"
    to.image = "acceptance-tests"
    container {
        mainClass = application.mainClass.get()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}