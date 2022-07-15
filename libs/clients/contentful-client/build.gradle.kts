plugins {
    kotlin("jvm")
    id("com.apollographql.apollo3")
}

apollo {
    service("invest") {
        srcDir("src/main/httpx/invest")
        packageName.set("no.arcane.platform.cms.space.invest")
    }
    service("legal") {
        srcDir("src/main/httpx/legal")
        packageName.set("no.arcane.platform.cms.space.legal")
    }
    service("research") {
        srcDir("src/main/httpx/research")
        packageName.set("no.arcane.platform.cms.space.research")
    }
}

dependencies {
    implementation("com.apollographql.apollo3:apollo-runtime:_")
}