#!/usr/bin/env bash

#
#  Run acceptance tests
#

./gradlew installDist --parallel

# env = gcp
docker compose up --build --abort-on-container-exit

# env = github
# docker compose -f docker-compose.yaml -f docker-compose.github.yaml up --build --abort-on-container-exit

# env = gcp | without ESP
# docker compose -f docker-compose.no-esp.yaml up --build --abort-on-container-exit
# docker compose -f docker-compose.no-esp.yaml up --build