# Testing OpenAPI spec

## Test API

* Update the open api spec in the test file - [arcane-platform-test-api.yaml](../libs/clients/arcane-platform-client/src/main/openapi/arcane-platform-test-api.yaml).
* Deploy updated test api spec.
```shell
./infra/gcp/deploy-test-endpoints-service.sh
```
* Perform acceptance tests as mentioned in [at.md](at.md).

## Canary API

* Diff this test api file with canary api file - [arcane-platform-canary-api.yaml](../libs/clients/arcane-platform-client/src/main/openapi/arcane-platform-canary-api.yaml).
  Only these values should differ:
  * `info/*`
  * `host`
  * `x-google-backend/*`
  * `paths/contentfulEvents/post/security`
  * `securityDefinitions/`
* Deploy updated canary spec and test using feature toggle in website.
```shell
./infra/gcp/deploy-canary-espv2.sh
```

## Main API
* Diff canary api file with main api file - [arcane-platform-api.yaml](../libs/clients/arcane-platform-client/src/main/openapi/arcane-platform-api.yaml).
  Only these values should differ:
  * `info/*`
  * `host`
  * `x-google-backend/*`
```shell
./infra/gcp/deploy-espv2.sh
```
