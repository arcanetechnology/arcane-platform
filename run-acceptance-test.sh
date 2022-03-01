#!/usr/bin/env bash

#
#  Run acceptance tests
#

./gradlew installDist --parallel

docker compose up --build --abort-on-container-exit

# without ESP
# docker compose -f docker-compose.no-esp.yaml up --build --abort-on-container-exit
# docker compose -f docker-compose.no-esp.yaml up --build