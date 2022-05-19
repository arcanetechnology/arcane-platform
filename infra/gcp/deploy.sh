#!/usr/bin/env bash

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

declare -A backendCloudRun
backendCloudRun["service"]="arcane-platform"
backendCloudRun["image"]="europe-docker.pkg.dev/${GCP_PROJECT_ID}/platform/arcane-platform-app:$(git rev-parse HEAD | cut -c 1-12)"
backendCloudRun["service_account"]="arcane-platform"

echo "Build with Gradle"
./gradlew :apps:arcane-platform-app:installDist --parallel

echo "Building and pushing docker image: ${backendCloudRun["image"]}"
docker image build --platform linux/amd64 -t "${backendCloudRun["image"]}" apps/arcane-platform-app
docker image push "${backendCloudRun["image"]}"

echo "Deploying to cloud run: ${backendCloudRun["image"]}"
gcloud run deploy "${backendCloudRun["service"]}" \
  --region europe-west1 \
  --image "${backendCloudRun["image"]}" \
  --cpu=1 \
  --memory=1Gi \
  --min-instances=1 \
  --max-instances=1 \
  --concurrency=1000 \
  --set-env-vars=GCP_PROJECT_ID="${GCP_PROJECT_ID}",GOOGLE_CLOUD_PROJECT="${GCP_PROJECT_ID}",INVEST_DENIED_COUNTRY_CODE_LIST="${INVEST_DENIED_COUNTRY_CODE_LIST}",INVEST_EMAIL_FROM="${INVEST_EMAIL_FROM}",INVEST_EMAIL_TO="${INVEST_EMAIL_TO}" \
  --service-account "${backendCloudRun["service_account"]}" \
  --no-allow-unauthenticated \
  --port=8080 \
  --platform=managed
