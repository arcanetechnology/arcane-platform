plugins {
    kotlin("jvm")
    id("com.apollographql.apollo3")
}

apollo {
    service("invest") {
        srcDir("src/main/httpx/invest")
        packageName.set("com.k33.platform.cms.space.invest")
    }
    service("research") {
        srcDir("src/main/httpx/research")
        packageName.set("com.k33.platform.cms.space.research")
    }
}

dependencies {
    implementation("com.apollographql.apollo3:apollo-runtime:_")
}