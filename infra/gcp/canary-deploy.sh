#!/usr/bin/env bash

#
#  Script to canary deploy k33-backend to GCP cloud run.
#

set -e

if [ -z "${BASH_VERSINFO}" ] || [ -z "${BASH_VERSINFO[0]}" ] || [ ${BASH_VERSINFO[0]} -lt 4 ]; then
  echo "This script requires Bash version >= 4"
  exit 1
fi

if [ -f .env.gcp ]; then
  set -o allexport
  source .env.gcp
  set +o allexport
fi

declare -A backendCloudRun
backendCloudRun["service"]="k33-backend"
backendCloudRun["image"]="europe-docker.pkg.dev/${GCP_PROJECT_ID}/backend/k33-backend:$(git rev-parse HEAD | cut -c 1-12)"
backendCloudRun["service_account"]="k33-backend"

echo "Build with Gradle"
./gradlew :apps:k33-backend:installDist --parallel

echo "Building and pushing docker image: ${backendCloudRun["image"]}"
docker image build --platform linux/amd64 -t "${backendCloudRun["image"]}" apps/k33-backend
docker image push "${backendCloudRun["image"]}"

echo "Canary deploying to cloud run: ${backendCloudRun["image"]}"
gcloud run deploy "${backendCloudRun["service"]}" \
  --region europe-west1 \
  --image "${backendCloudRun["image"]}" \
  --cpu=1 \
  --memory=1Gi \
  --min-instances=1 \
  --max-instances=1 \
  --concurrency=1000 \
  --set-env-vars=GCP_PROJECT_ID="${GCP_PROJECT_ID}" \
  --set-env-vars=GOOGLE_CLOUD_PROJECT="${GCP_PROJECT_ID}" \
  --set-env-vars=SLACK_ALERTS_CHANNEL_ID="${SLACK_ALERTS_CHANNEL_ID}" \
  --set-env-vars=SLACK_GENERAL_CHANNEL_ID="${SLACK_GENERAL_CHANNEL_ID}" \
  --set-env-vars=SLACK_INVEST_CHANNEL_ID="${SLACK_INVEST_CHANNEL_ID}" \
  --set-env-vars=SLACK_PRODUCT_CHANNEL_ID="${SLACK_PRODUCT_CHANNEL_ID}" \
  --set-env-vars=SLACK_PROFESSIONAL_INVESTORS_CHANNEL_ID="${SLACK_PROFESSIONAL_INVESTORS_CHANNEL_ID}" \
  --set-env-vars=SLACK_RESEARCH_CHANNEL_ID="${SLACK_RESEARCH_CHANNEL_ID}" \
  --set-env-vars=SLACK_RESEARCH_EVENTS_CHANNEL_ID="${SLACK_RESEARCH_EVENTS_CHANNEL_ID}" \
  --set-env-vars=^:^INVEST_DENIED_COUNTRY_CODE_LIST="${INVEST_DENIED_COUNTRY_CODE_LIST}" \
  --set-env-vars=INVEST_EMAIL_FROM="${INVEST_EMAIL_FROM}" \
  --set-env-vars=^:^INVEST_EMAIL_TO_LIST="${INVEST_EMAIL_TO_LIST}" \
  --set-env-vars=^:^INVEST_EMAIL_CC_LIST="${INVEST_EMAIL_CC_LIST}" \
  --set-env-vars=^:^INVEST_EMAIL_BCC_LIST="${INVEST_EMAIL_BCC_LIST}" \
  --service-account "${backendCloudRun["service_account"]}" \
  --no-allow-unauthenticated \
  --port=8080 \
  --tag canary \
  --no-traffic \
  --platform=managed
