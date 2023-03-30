# K33 Platform

[![Kotlin](https://img.shields.io/badge/kotlin-1.8.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Test](https://github.com/33k33/k33-platform/actions/workflows/test.yaml/badge.svg?branch=main)](https://github.com/33k33/k33-platform/actions/workflows/test.yaml)
![GitHub Workflow Status (with branch)](https://img.shields.io/github/actions/workflow/status/33k33/k33-platform/test.yaml?branch=main&logo=github)
![GitHub](https://img.shields.io/github/license/33k33/k33-platform)

[Common Gradle Commands](./docs/gradle.md)

[Setup to run GitHub Actions Locally](./.github/workflows/README.md)

## Apps

[K33 Backend App](apps/k33-backend/README.md)

[Acceptance Tests](docs/at.md)  
Runs entire system, which include web apps, gateways, databases, 3rd party service emulators, API test clients, in docker-compose and runs integration tests.

## Cloud Infra

 * [GCP](./infra/gcp/README.md)
 * [Azure](./infra/azure/SETUP.md) for social `Login with Microsoft`

[Canary Deployment](docs/canary.md)  
With `dev` and `prod` environment options, we also have `canary` and `main` deployment options under `prod` environment.

## Tech stack

 * [Kotlin](https://kotlinlang.org/)
   * [Ktor](https://ktor.io/)
   * [Arrow-kt](https://arrow-kt.io/)
   * [JDK](https://adoptium.net/)
 * [GCP](https://cloud.google.com/)
   * [ESPv2](https://cloud.google.com/endpoints/docs/openapi/architecture-overview)
   * [Cloud Run](https://cloud.google.com/run/docs/overview/what-is-cloud-run) (KNative on k8s)
   * [Firestore](https://cloud.google.com/firestore/docs/data-model)
   * [Spanner](https://cloud.google.com/spanner)
 * [Docker](https://www.docker.com/)
   * [Docker compose](https://docs.docker.com/compose/)
 * [Gradle](https://gradle.org/)
   * [Kotlin Script](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
 * Structured/Modular  Monolith
 * [Monorepo](https://en.wikipedia.org/wiki/Monorepo)
 * API
   * [REST](https://cloud.google.com/apis/design/resources)
   * [GraphQL](https://graphql.org/)
   * [gRPC](https://grpc.io/)

## Folder structure

* `apps` - Executable tools / server apps
  * `k33-backend` - Dockerized structured monolith application which bundles functional product apps and feature services, which can be spun into their own microservices in the future, when needed.
  * `acceptance-test` - Dockerized application, representing test-client for k33-backend app.
  * `firestore-admin` - Admin tool to perform DB maintenance operations 
* `infra` - Infrastructure scripts / setup documentation
  * `gcp`
* `libs` - Libraries that are added to executable `apps` 
  * `apps` - Functional Product apps on K33 Platform (Non-executable libraries)  
  * `clients` - API Clients of k33-backend and external services
  * `services` - Functional Services representing major features which are part of `product apps` or implicit `platform app`. 
  * `utils` - Low-level, non-functional, cross-cutting, utility libraries used in higher-order libraries. 