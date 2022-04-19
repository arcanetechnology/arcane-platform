plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(platform("com.google.cloud:libraries-bom:_"))
    implementation("com.google.cloud:google-cloud-storage")

    implementation(project(":libs:utils:google-coroutine-ktx"))
    implementation(KotlinX.coroutines.core)

    implementation(project(":libs:utils:config"))
    implementation(project(":libs:utils:logging"))
}