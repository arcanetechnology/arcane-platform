
plugins {
    application
    kotlin("jvm")
    id("com.google.cloud.tools.jib")
}

dependencies {
    runtimeOnly(kotlin("stdlib-jdk8"))

    runtimeOnly(project(":libs:services:identity"))
    runtimeOnly(project(":libs:services:terms-and-conditions"))

    runtimeOnly(project(":libs:utils:ktor"))
    runtimeOnly("io.ktor:ktor-server-netty:${Version.ktor}")
    runtimeOnly(project(":libs:utils:logging"))
    runtimeOnly("io.micronaut.gcp:micronaut-gcp-logging:${Version.micronaut}")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    applicationName = "arane-platform-app"
    applicationDefaultJvmArgs = listOf("-Dlogback.configurationFile=logback.xml")
}

jib {
    from.image = "eclipse-temurin:17.0.1_12-jdk-alpine"
    to.image = "arcane-platform-app"
    container {
        mainClass = application.mainClass.get()
        jvmFlags = listOf("-Dlogback.configurationFile=logback.gcp.xml")
    }
}

tasks.withType<JavaExec> {
    environment("ENV", "local")
}