plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation(project(":libs:utils:config"))
    implementation(project(":libs:utils:logging"))

    implementation(project(":libs:utils:cms:cms-api"))
    implementation("com.contentful.java:java-sdk:_")
    implementation("com.github.contentful.rich-text-renderer-java:html:_")
}