# Arcane GCP Platform App

This app is targeted for GCP.
It may use GCP specific services such as ESP, Firestore, KMS, PubSub etc.

## How to build

Directly run App

    ./gradlew :apps:arcane-gcp-platform-app:run

Build docker image using `jib` tool with files in `build-jib-cache`.

    ./gradlew :apps:arcane-gcp-platform-app:jibDockerBuild

Build docker & push image using `jib` tool with files in `build-jib-cache`.

    ./gradlew :apps:arcane-gcp-platform-app:jib -Djib.to.image=<image>

## Checking for dependency resolution

    ./gradlew :apps:arcane-gcp-platform-app:dependencyInsight --configuration runtimeClasspath --dependency dependency-name