# Common Gradle commands

## Check for new version of dependencies

```shell
./gradlew refreshVersions
```

## Checking for dependency resolution

```shell
./gradlew :apps:k33-backend:dependencyInsight --configuration runtimeClasspath --dependency dependency-name
```

## Update grraphql schema files

```shell
./gradlew :libs:clients:contentful-client:downloadApolloSchema --endpoint="https://graphql.contentful.com/content/v1/spaces/${INVEST_SPACE_ID}" --schema=libs/clients/contentful-client/src/main/httpx/invest/schema.graphqls --header="Authorization: Bearer ${INVEST_SPACE_TOKEN}"
./gradlew :libs:clients:contentful-client:downloadApolloSchema --endpoint="https://graphql.contentful.com/content/v1/spaces/${LEGAL_SPACE_ID}" --schema=libs/clients/contentful-client/src/main/httpx/legal/schema.graphqls --header="Authorization: Bearer ${LEGAL_SPACE_TOKEN}"
./gradlew :libs:clients:contentful-client:downloadApolloSchema --endpoint="https://graphql.contentful.com/content/v1/spaces/${RESEARCH_SPACE_ID}" --schema=libs/clients/contentful-client/src/main/httpx/research/schema.graphqls --header="Authorization: Bearer ${RESEARCH_SPACE_TOKEN}"
```