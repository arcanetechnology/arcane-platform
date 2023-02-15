plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project("payment-endpoint"))
    implementation(project("stripe"))
}