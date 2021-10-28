rootProject.name = "arcane-platform"

include(
    // apps
    "apps:acceptance-tests",
    "apps:arcane-platform-app",
    "apps:arcane-gcp-platform-app",
    "apps:tools",
    // libs
    // libs / clients
    "libs:clients:arcane-platform-client",
    // libs / modules
    "libs:modules:identity",
    // libs / services
    "libs:services:email",
    "libs:services:payments",
    // libs / utils
    "libs:utils:config",
    "libs:utils:ktor",
    "libs:utils:logging",
    "libs:utils:metrics",
)

