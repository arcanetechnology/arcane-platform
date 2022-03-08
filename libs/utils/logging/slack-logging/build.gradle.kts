plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation(project(":libs:utils:logging:marker-api"))
    implementation("ch.qos.logback:logback-classic:_")

    implementation(project(":libs:utils:slack"))
    implementation(KotlinX.coroutines.slf4j)

    implementation("org.slf4j:slf4j-api:_")
    implementation("ch.qos.logback:logback-classic:_")
    implementation("ch.qos.logback.contrib:logback-json-classic:_")

    // test
    testImplementation(project(":libs:utils:logging"))
    testImplementation("io.kotest:kotest-runner-junit5-jvm:_")
}