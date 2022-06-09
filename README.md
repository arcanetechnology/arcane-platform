# Arcane Platform

[![Kotlin](https://img.shields.io/badge/kotlin-1.7.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/arcanetechnology/arcane-platform/Test/main?logo=github)
![GitHub](https://img.shields.io/github/license/arcanetechnology/arcane-platform)

[Developer Notes](./docs/dev.md)

[Common Gradle Commands](./docs/gradle.md)

[Setup to run GitHub Actions Locally](./.github/workflows/README.md)

## Apps

[Platform](apps/arcane-platform-app/README.md)

### Tech stack

 * [Kotlin](https://kotlinlang.org/)
   * [Ktor](https://ktor.io/)
   * [Arrow-kt](https://arrow-kt.io/)
   * JDK
 * GCP
   * OpenAPI v2 + [ESPv2](https://cloud.google.com/endpoints/docs/openapi/architecture-overview)
   * [Cloud Run](https://cloud.google.com/run) ([KNative](https://knative.dev/docs/))
   * [Firestore](https://cloud.google.com/firestore) DB + [firestore4k](https://firestore4k.io)
   * [Spanner](https://cloud.google.com/spanner) DB
   * [Firebase Auth](https://firebase.google.com/docs/auth) + [Identity Platform](https://cloud.google.com/identity-platform)
     * Login with Google
     * Login with Apple ID
     * SSO using SAML
   * [Cloud Armor](https://cloud.google.com/armor)
 * [Docker](https://www.docker.com/)
   * [Docker compose](https://docs.docker.com/compose/)
 * [Gradle](https://gradle.org/)
   * [Kotlin Script](https://gradle.org/kotlin/)
 * Structured Monolith
 * Monorepo
