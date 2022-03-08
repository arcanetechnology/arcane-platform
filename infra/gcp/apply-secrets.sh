#!/usr/bin/env bash

#
#  Script to apply secrets for arcane-platform-app to GCP cloud run.
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

declare -A gcp_secrets

gcp_secrets[0]="SENDGRID_API_KEY"
gcp_secrets[1]="LEGAL_SPACE_ID"
gcp_secrets[2]="LEGAL_SPACE_TOKEN"
gcp_secrets[3]="RESEARCH_SPACE_ID"
gcp_secrets[4]="RESEARCH_SPACE_TOKEN"
gcp_secrets[5]="ALGOLIA_APP_ID"
gcp_secrets[6]="ALGOLIA_API_KEY"
gcp_secrets[7]="SLACK_TOKEN"

CLOUD_RUN_SERVICE="arcane-platform"

echo Update cloud run service: "${CLOUD_RUN_SERVICE}" with secrets

index=0

secret_string=""

while [ -n "${gcp_secrets["$index"]}" ]; do

  echo Setting secret: "${gcp_secrets["$index"]}" in cloud run service "${CLOUD_RUN_SERVICE}"

  if [ -n "${secret_string}" ]; then
    secret_string+=","
  fi

  secret_string+="${gcp_secrets["$index"]}=${gcp_secrets["$index"]}:latest"

  index=$((index + 1))

done

echo $secret_string

gcloud run services update "${CLOUD_RUN_SERVICE}" \
  --update-secrets="${secret_string}" \
  --region europe-west1