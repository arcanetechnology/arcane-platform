plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(project(":libs:utils:logging:marker-api"))

    api("org.slf4j:slf4j-api:_")
    runtimeOnly("ch.qos.logback:logback-classic:_")

    implementation(KotlinX.coroutines.slf4j)
}