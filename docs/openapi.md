# Testing OpenAPI spec

## Test API

* Update the open api spec in the test file - [k33-backend-test-api.yaml](../libs/clients/k33-backend-client/src/main/openapi/k33-backend-test-api.yaml).
* Deploy updated test api spec.
```shell
./infra/gcp/deploy-test-endpoints-service.sh
```
* Perform acceptance tests as mentioned in [at.md](at.md).

## Canary API

* Diff this test api file with canary api file - [k33-backend-canary-api.yaml](../libs/clients/k33-backend-client/src/main/openapi/k33-backend-canary-api.yaml).
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
* Diff canary api file with main api file - [k33-backend-api.yaml](../libs/clients/k33-backend-client/src/main/openapi/k33-backend-api.yaml).
  Only these values should differ:
  * `info/*`
  * `host`
  * `x-google-backend/*`
```shell
./infra/gcp/deploy-espv2.sh
```
