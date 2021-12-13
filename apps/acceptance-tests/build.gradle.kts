plugins {
    application
    kotlin("jvm")
    id("com.google.cloud.tools.jib")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":libs:utils:logging"))
    implementation("io.ktor:ktor-client-cio:${Version.ktor}")
    implementation("io.ktor:ktor-client-logging:${Version.ktor}")
    implementation("io.kotest:kotest-runner-junit5-jvm:${Version.kotest}")
    implementation("org.junit.platform:junit-platform-console:${Version.junit5}")
}

application {
    mainClass.set("org.junit.platform.console.ConsoleLauncher")
}

jib {
    from.image = "eclipse-temurin:17.0.1_12-jdk-alpine"
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