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

    implementation("com.google.firebase:firebase-admin:_")
    implementation(project(":libs:utils:google-coroutine-ktx"))

    implementation(project(":libs:utils:graphql"))

    implementation(platform("com.google.cloud:libraries-bom:_"))
    implementation("com.google.cloud:google-cloud-spanner")

    implementation("org.postgresql:postgresql:_")
    implementation("com.zaxxer:HikariCP:_")

    implementation("io.arrow-kt:arrow-core:_")

    implementation(KotlinX.serialization.core)
    implementation(KotlinX.serialization.json)

    // test
    testImplementation(project("test-db-setup"))
    testImplementation("io.kotest:kotest-runner-junit5-jvm:_")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:_")
    testImplementation("org.testcontainers:postgresql:_")
    testImplementation("org.testcontainers:gcloud:_")
    testImplementation("io.ktor:ktor-server-test-host:_")
    testImplementation("io.ktor:ktor-server-content-negotiation:_")
    testImplementation("io.ktor:ktor-client-content-negotiation:_")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:_")
    testImplementation("io.ktor:ktor-client-logging:_")
    testImplementation("io.kotest:kotest-assertions-json-jvm:_")
}