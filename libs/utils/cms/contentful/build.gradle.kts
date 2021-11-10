plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation(project(":libs:utils:config"))
    implementation(project(":libs:utils:logging"))

    implementation(project(":libs:utils:cms:cms-api"))
    implementation("com.contentful.java:java-sdk:10.5.2")
    implementation("com.github.contentful.rich-text-renderer-java:html:1.1.0")
}