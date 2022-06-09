plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":libs:utils:config"))
    implementation(project(":libs:utils:logging"))
    implementation(project(":libs:utils:slack"))
    implementation(project(":libs:utils:arrow-ktx"))

    implementation(Ktor.server.core)
    implementation("io.ktor:ktor-server-auth:_")

    implementation(project(":libs:services:identity"))
    implementation(project(":libs:services:user:user-model"))

    implementation(project(":libs:utils:graphql"))

    implementation("org.postgresql:postgresql:_")
    implementation("com.zaxxer:HikariCP:_")

    implementation("io.arrow-kt:arrow-core:_")

    // test
    testImplementation("io.kotest:kotest-runner-junit5-jvm:_")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:_")
    testImplementation("org.testcontainers:postgresql:_")
    testImplementation("io.kotest:kotest-assertions-json-jvm:_")
}