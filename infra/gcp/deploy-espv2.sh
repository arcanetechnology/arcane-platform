#!/usr/bin/env bash

#
#  Script to deploy esp v2 to GCP cloud run.
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
trap 'rm -rf "$TMP_DIR"' EXIT

files=(admin invest misc platform trade-admin webhook)

files_string=""

for file in "${files[@]}"; do
  sed 's~${GCP_PROJECT_ID}~'"${GCP_PROJECT_ID}"'~g; s~${GCP_API_HOST}~'"${GCP_API_HOST}"'~g; s~${GCP_BACKEND_HOST}~'"${GCP_BACKEND_HOST}"'~g' \
    libs/clients/arcane-platform-client/src/main/openapi/api/${file}.yaml > "$TMP_DIR/${file}.yaml"
  files_string+=" ${TMP_DIR}/${file}.yaml"
done

gcloud endpoints services deploy ${files_string}

# Build ESP docker

## define ESP docker name and tag

declare -A espCloudRun
espCloudRun["service"]="arcane-platform-gateway"
espCloudRun["endpoint_service"]="${GCP_API_HOST}"
espCloudRun["service_account"]="arcane-platform-gateway"

echo "espCloudRun[service]: ${espCloudRun["service"]}"
echo "espCloudRun[endpoint_service]: ${espCloudRun["endpoint_service"]}"
echo "espCloudRun[service_account]: ${espCloudRun["service_account"]}"

# Default to the latest released ESPv2 version.
BASE_IMAGE_NAME="gcr.io/endpoints-release/endpoints-runtime-serverless"
ESP_TAG="2"

echo "Determining fully-qualified ESP version for tag: ${ESP_TAG}"

ALL_TAGS=$(gcloud container images list-tags "${BASE_IMAGE_NAME}" \
  --filter="tags~^${ESP_TAG}$" \
  --format="value(tags)")
IFS=',' read -ra TAGS_ARRAY <<<"${ALL_TAGS}"

if [ ${#TAGS_ARRAY[@]} -eq 0 ]; then
  error_exit "Did not find ESP version: ${ESP_TAG}"
fi

# Find the tag with the longest length.
ESP_FULL_VERSION=""
for tag in "${TAGS_ARRAY[@]}"; do
  if [ ${#tag} -gt ${#ESP_FULL_VERSION} ]; then
    ESP_FULL_VERSION=${tag}
  fi
done
echo "ESP_FULL_VERSION: ${ESP_FULL_VERSION}"

ENDPOINT_SERVICE="${espCloudRun["endpoint_service"]}"

echo "ENDPOINT_SERVICE: ${ENDPOINT_SERVICE}"

espCloudRun["service_config"]="$(gcloud endpoints configs list --service "$ENDPOINT_SERVICE" --format=object --flatten=id --sort-by=~id --limit=1)"
echo "service_config: ${espCloudRun["service_config"]}"

espCloudRun["image"]="europe-docker.pkg.dev/${GCP_PROJECT_ID}/platform/endpoints-runtime-serverless:${ESP_FULL_VERSION}-${espCloudRun["endpoint_service"]}-${espCloudRun["service_config"]}"

echo "espCloudRun[image]: ${espCloudRun["image"]}"

## build and push ESP docker image
./apps/esp-v2/build_docker_image.sh \
  -s "${espCloudRun["endpoint_service"]}" \
  -c "${espCloudRun["service_config"]}" \
  -p "$GCP_PROJECT_ID" \
  -v "$ESP_FULL_VERSION" \
  -g europe-docker.pkg.dev/"$GCP_PROJECT_ID"/platform

# Deploy ESP to GCP Cloud Run
gcloud run deploy "${espCloudRun["service"]}" \
  --region europe-west1 \
  --image "${espCloudRun["image"]}" \
  --set-env-vars=ESPv2_ARGS=^++^--cors_preset=cors_with_regex++--cors_allow_origin_regex="${CORS_REGEX}"++--cors_max_age=5m \
  --cpu=1 \
  --memory=512Mi \
  --min-instances=1 \
  --max-instances=1 \
  --concurrency=1000 \
  --service-account "${espCloudRun["service_account"]}" \
  --allow-unauthenticated \
  --port=8080 \
  --platform=managed
