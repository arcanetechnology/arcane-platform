plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":libs:utils:config"))
    implementation(project(":libs:utils:logging"))

    implementation("io.ktor:ktor-client-cio:${Version.ktor}")
    implementation("io.ktor:ktor-client-logging:${Version.ktor}")
    implementation("io.ktor:ktor-client-jackson:${Version.ktor}") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }

    implementation("io.jsonwebtoken:jjwt-api:${Version.jjwt}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${Version.jjwt}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${Version.jjwt}")

    implementation("io.arrow-kt:arrow-core:${Version.arrow}")
}