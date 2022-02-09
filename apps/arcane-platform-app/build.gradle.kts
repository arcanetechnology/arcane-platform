plugins {
    application
    kotlin("jvm")
    id("com.google.cloud.tools.jib")
}

dependencies {
    runtimeOnly(kotlin("stdlib-jdk8"))

    runtimeOnly(project(":libs:services:identity"))
    runtimeOnly(project(":libs:services:user"))
    runtimeOnly(project(":libs:services:terms-and-conditions"))

    runtimeOnly(project(":libs:utils:ktor"))
    runtimeOnly(Ktor.server.netty)
    runtimeOnly(project(":libs:utils:logging:gcp-logging"))
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    applicationName = "arane-platform-app"
    applicationDefaultJvmArgs = listOf("-Dlogback.configurationFile=logback.xml")
}

jib {
    from.image = "azul/zulu-openjdk:17.0.2-17.32.13"
    to.image = "arcane-platform-app"
    container {
        mainClass = application.mainClass.get()
        jvmFlags = listOf("-Dlogback.configurationFile=logback.gcp.xml")
    }
}

tasks.withType<JavaExec> {
    environment("ENV", "local")
}