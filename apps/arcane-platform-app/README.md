# Arcane GCP Platform App

This app is targeted for GCP.
It may use GCP specific services such as ESP, Firestore, KMS, PubSub etc.

## How to build

Directly run App

    ./gradlew :apps:arcane-platform-app:run

## Checking for dependency resolution

    ./gradlew :apps:arcane-platform-app:dependencyInsight --configuration runtimeClasspath --dependency dependency-name