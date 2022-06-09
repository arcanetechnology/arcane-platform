import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
    id("co.uzzu.dotenv.gradle")
    id("com.apollographql.apollo3") apply false
}

allprojects {
    group = "no.arcane.platform"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        // needed for contentful sdk
        maven { url = uri("https://jitpack.io") }
        // needed for firestore4k
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_18.toString()
        targetCompatibility = JavaVersion.VERSION_18.toString()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_18.majorVersion
            freeCompilerArgs += "-Xcontext-receivers"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        environment = env.allVariables
    }
}

subprojects {
    // Address https://github.com/gradle/gradle/issues/4823: Force parent project evaluation before sub-project evaluation for Kotlin build scripts
    if (gradle.startParameter.isConfigureOnDemand
        && buildscript.sourceFile?.extension?.toLowerCase() == "kts"
        && parent != rootProject) {
        generateSequence(parent) { project -> project.parent.takeIf { it != rootProject } }
            .forEach { evaluationDependsOn(it.path) }
    }
}