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

files=(admin invest misc platform trade-admin webhook)

files_string=""

for file in "${files[@]}"; do
  files_string+=" libs/clients/arcane-platform-client/src/main/openapi/test/${file}.yaml"
done

gcloud endpoints services deploy ${files_string}