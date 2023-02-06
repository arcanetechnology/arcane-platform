# K33 Backend GCP App

This app is targeted for GCP.
It may use GCP specific services such as ESP, Firestore, KMS, PubSub etc.

## How to build

Directly run App

    ./gradlew :apps:k33-backend:run

## Checking for dependency resolution

    ./gradlew :apps:k33-backend:dependencyInsight --configuration runtimeClasspath --dependency dependency-name