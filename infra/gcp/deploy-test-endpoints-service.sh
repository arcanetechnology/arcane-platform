#!/usr/bin/env bash

#
#  Script to deploy endpoints service.
#

set -e

#### checking bash version
if [ -z "${BASH_VERSINFO}" ] || [ -z "${BASH_VERSINFO[0]}" ] || [ ${BASH_VERSINFO[0]} -lt 4 ]; then
  echo "This script requires Bash version >= 4"
  exit 1
fi

#### init env vars from .env
if [ -f .env.gcp ]; then
  set -o allexport
  source .env.gcp
  set +o allexport
fi

# Deploy endpoints service

## temp dir to store modified OpenAPI file
TMP_DIR=$(mktemp -d)
echo "$TMP_DIR"
TMP_FILE="$TMP_DIR/k33-backend-test-api.yaml"
trap 'rm -rf "$TMP_DIR"' EXIT

sed 's~${GCP_PROJECT_ID}~'"${GCP_PROJECT_ID}"'~g' libs/clients/k33-backend-client/src/main/openapi/k33-backend-test-api.yaml >"$TMP_FILE"

gcloud endpoints services deploy "$TMP_FILE"
