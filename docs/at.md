# Acceptance Tests

* Gradle Build
```shell
./gradlew installDist --parallel
```

* Run AT with ESPv2 using test spec
```shell
docker compose up --build --abort-on-container-exit
docker compose down
```