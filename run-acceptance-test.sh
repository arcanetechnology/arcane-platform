#!/usr/local/bin/bash

#
#  Run acceptance tests
#

if [ -z "${BASH_VERSINFO}" ] || [ -z "${BASH_VERSINFO[0]}" ] || [ ${BASH_VERSINFO[0]} -lt 4 ]; then
  echo "This script requires Bash version >= 4"
  exit 1
fi

if [ -f .env ]; then
  set -o allexport
  source .env
  set +o allexport
fi

./gradlew :apps:acceptance-tests:jibDockerBuild
./gradlew :apps:arcane-gcp-platform-app:jibDockerBuild

# env = local
# docker compose -f docker-compose.yaml up --abort-on-container-exit

# env = github
docker compose -f docker-compose.github.yaml up --abort-on-container-exit

# env = gcp
docker compose up --abort-on-container-exit
