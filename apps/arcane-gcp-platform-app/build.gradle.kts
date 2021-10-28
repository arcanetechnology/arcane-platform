
plugins {
    application
    kotlin("jvm")
    id("com.google.cloud.tools.jib")
}

dependencies {
    runtimeOnly(kotlin("stdlib-jdk8"))

    runtimeOnly(project(":libs:modules:identity"))

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
    from.image = "adoptopenjdk/openjdk16-openj9:x86_64-alpine-jdk-16.0.1_9_openj9-0.26.0"
    to.image = "arcane-platform-app"
    container {
        mainClass = application.mainClass.get()
        jvmFlags = listOf("-Dlogback.configurationFile=logback.gcp.xml")
    }
}