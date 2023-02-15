plugins {
    id("de.fayard.refreshVersions") version "0.40.1"
}

refreshVersions {
    rejectVersionIf {
        candidate.stabilityLevel != de.fayard.refreshVersions.core.StabilityLevel.Stable
    }
    extraArtifactVersionKeyRules(file("refreshVersions-extra-rules.txt"))
}

rootProject.name = "k33-platform"

include(
    // apps
    "apps:acceptance-tests",
    "apps:k33-backend",

    "apps:firestore-admin",

    "apps:oauth2-provider-emulator",
    "apps:oauth2-provider-emulator:oauth2-provider-api",

    // libs

    // libs / apps
    "libs:apps:invest",

    // libs / clients
    "libs:clients:k33-backend-client",
    "libs:clients:contentful-client",

    // libs / services

    "libs:services:email",
    "libs:services:email:email-api",
    "libs:services:email:sendgrid",

    "libs:services:email-subscription",

    "libs:services:identity",
    "libs:services:identity:apple",
    "libs:services:identity:identity-api",
    "libs:services:identity:gcp",

    "libs:services:payment",
    "libs:services:payment:payment-endpoint",
    "libs:services:payment:stripe",

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
