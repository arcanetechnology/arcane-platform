plugins {
    application
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":libs:utils:logging"))
}

application {
    mainClass.set("no.arcane.tools.StripeUsersKt")
}
