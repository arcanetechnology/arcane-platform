plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":libs:utils:config"))
    implementation(project(":libs:utils:logging"))
    implementation(project(":libs:utils:slack"))

    implementation(project(":libs:utils:cms:cms-api"))
    implementation("com.contentful.java:java-sdk:_")
    implementation("com.github.contentful.rich-text-renderer-java:html:_")

    implementation(Ktor.client.cio)
    implementation(Ktor.client.logging)
    implementation(Ktor.client.serialization)

    implementation(Ktor.server.core)

    implementation("net.andreinc:mapneat:_")
    implementation("com.jayway.jsonpath:json-path:_")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:_")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:_")
    implementation("org.apache.logging.log4j:log4j-core:_")

    implementation("com.algolia:algoliasearch-client-kotlin:_")

    // test
    testImplementation("io.kotest:kotest-runner-junit5-jvm:_")
}