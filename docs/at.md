# Acceptance Tests

* Build docker images
```shell
./gradlew jibDockerBuild
```

* Run AT with ESPv2 using test spec
```shell
docker-compose up --abort-on-container-exit
docker-compose down
```