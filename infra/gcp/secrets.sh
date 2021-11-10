#!/usr/local/bin/bash

#
#  Script to deploy arcane-platform-app to GCP cloud run.
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

gcp_secrets[0, "name"]="contentful_config"
gcp_secrets[0, "src"]="secrets/contentful-gcp.conf"
gcp_secrets[0, "target"]="/config/contentful-gcp.conf"

gcp_secrets[1, "name"]="sendgrid_api_key"
gcp_secrets[1, "src"]="secrets/sendgrid_api_key.txt"
gcp_secrets[1, "target"]="SENDGRID_API_KEY"

index=0

while [ -n "${gcp_secrets["$index", "name"]}" ]; do

  echo Creating secret: "${gcp_secrets["$index", "name"]}"

  gcloud secrets create "${gcp_secrets["$index", "name"]}" \
    --data-file="${gcp_secrets["$index", "src"]}" \
    --replication-policy=user-managed \
    --locations=europe-west1

  index=$((index + 1))

done

CLOUD_RUN_SERVICE="arcane-platform"

echo Update cloud run service: "${CLOUD_RUN_SERVICE}" with secrets

index=0

secret_string=""

while [ -n "${gcp_secrets["$index", "name"]}" ]; do

  echo Setting secret: "${gcp_secrets["$index", "name"]}" in cloud run service "${CLOUD_RUN_SERVICE}"

  if [ -n "${secret_string}" ]; then
    secret_string+=","
  fi

  secret_string+="${gcp_secrets["$index", "target"]}=${gcp_secrets["$index", "name"]}:latest"

  index=$((index + 1))

done

echo $secret_string

gcloud run services update "${CLOUD_RUN_SERVICE}" \
  --update-secrets="${secret_string}" \
  --region europe-west1
