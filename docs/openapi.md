# Testing OpenAPI spec

* Update the open api spec in the test file - [arcane-platform-test-api.yaml](../libs/clients/arcane-platform-client/src/main/openapi/arcane-platform-test-api.yaml).
* Diff this file with main file - [arcane-platform-api.yaml](../libs/clients/arcane-platform-client/src/main/openapi/arcane-platform-api.yaml).
* Only these values should differ:
  * `host`
  * `x-google-backend`
  * `securityDefinitions/firebase`
* Deploy updated test spec
```shell
./infra/gcp/deploy-test-endpoints-service.sh
```
* Perform acceptance tests as mentioned in [at.md](at.md).