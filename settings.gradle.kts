plugins {
    id("de.fayard.refreshVersions") version "0.40.1"
}

refreshVersions {
    rejectVersionIf {
        candidate.stabilityLevel != de.fayard.refreshVersions.core.StabilityLevel.Stable
    }
    extraArtifactVersionKeyRules(file("refreshVersions-extra-rules.txt"))
}

rootProject.name = "arcane-platform"

include(
    // apps
    "apps:acceptance-tests",
    "apps:arcane-platform-app",

    "apps:firestore-admin",

    "apps:oauth2-provider-emulator",
    "apps:oauth2-provider-emulator:oauth2-provider-api",

    // libs

    // libs / apps
    "libs:apps:admin",
    "libs:apps:invest",
    "libs:apps:trade",
    "libs:apps:trade:test-db-setup",

    // libs / clients
    "libs:clients:arcane-platform-client",
    "libs:clients:contentful-client",

    // libs / services

    "libs:services:email",
    "libs:services:email:email-api",
    "libs:services:email:sendgrid",

    "libs:services:identity",
    "libs:services:identity:admin-auth",
    "libs:services:identity:apple",
    "libs:services:identity:identity-api",
    "libs:services:identity:gcp",

    "libs:services:terms-and-conditions",
    "libs:services:terms-and-conditions:terms-and-conditions-endpoint",
    "libs:services:terms-and-conditions:terms-and-conditions-graphql",
    "libs:services:terms-and-conditions:terms-and-conditions-service",

    "libs:services:user",
    "libs:services:user:user-analytics",
    "libs:services:user:user-endpoint",
    "libs:services:user:user-graphql",
    "libs:services:user:user-model",
    "libs:services:user:user-service",

    // libs / utils

    "libs:utils:analytics",

    "libs:utils:arrow-ktx",

    "libs:utils:cms",
    "libs:utils:cms:cms-api",
    "libs:utils:cms:contentful",

    "libs:utils:config",
    "libs:utils:file-store",
    "libs:utils:firebase-auth",
    "libs:utils:google-coroutine-ktx",
    "libs:utils:graphql",

    "libs:utils:ktor",

    "libs:utils:logging",
    "libs:utils:logging:gcp-logging",
    "libs:utils:logging:marker-api",
    "libs:utils:logging:slack-logging",

    "libs:utils:metrics",
    "libs:utils:slack",
)
