plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":libs:utils:config"))
    implementation(project(":libs:utils:logging"))
    implementation(project(":libs:utils:slack"))

    implementation(Ktor.server.core)
    implementation("io.ktor:ktor-server-auth:_")

    implementation(project(":libs:services:identity"))
    implementation(project(":libs:services:user:user-model"))
    implementation(project(":libs:services:email"))

    implementation("com.googlecode.libphonenumber:libphonenumber:_")
    implementation(KotlinX.serialization.core)
    implementation(KotlinX.serialization.json)

    // test
    testImplementation("io.kotest:kotest-runner-junit5-jvm:_")
}