plugins {
    id("de.fayard.refreshVersions") version "0.40.1"
}

refreshVersions {
    rejectVersionIf {
        candidate.stabilityLevel != de.fayard.refreshVersions.core.StabilityLevel.Stable
    }
}

rootProject.name = "arcane-platform"

include(
    // apps
    "apps:acceptance-tests",
    "apps:arcane-platform-app",

    "apps:oauth2-provider-emulator",
    "apps:oauth2-provider-emulator:oauth2-provider-api",

    // libs
    // libs / clients
    "libs:clients:arcane-platform-client",

    // libs / services

    "libs:services:email",
    "libs:services:email:email-api",
    "libs:services:email:sendgrid",

    "libs:services:identity",
    "libs:services:identity:apple",
    "libs:services:identity:identity-api",
    "libs:services:identity:gcp",

    "libs:services:terms-and-conditions",
    "libs:services:user",
    "libs:services:user:model",

    // libs / utils

    "libs:utils:cms",
    "libs:utils:cms:cms-api",
    "libs:utils:cms:contentful",

    "libs:utils:config",
    "libs:utils:firebase-auth",

    "libs:utils:ktor",

    "libs:utils:logging",
    "libs:utils:logging:gcp-logging",

    "libs:utils:metrics",
)
