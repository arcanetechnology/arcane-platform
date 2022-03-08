plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation(project(":libs:utils:config"))
    implementation(project(":libs:utils:logging"))

    implementation("com.slack.api:slack-api-client:_")
    api("com.slack.api:slack-api-model-kotlin-extension:_")
    implementation("com.slack.api:slack-api-client-kotlin-extension:_")

    implementation(KotlinX.coroutines.core)
    implementation(KotlinX.coroutines.jdk8)

    // test
    testImplementation("io.kotest:kotest-runner-junit5-jvm:_")
}