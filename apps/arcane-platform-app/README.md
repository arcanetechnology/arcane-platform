# Arcane Platform App

## How to build

Build docker image using `jib` tool with files in `build-jib-cache`.

    ./gradlew :apps:arcane-platform-app:jibDockerBuild --parallel
