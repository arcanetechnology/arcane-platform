#!/usr/bin/env bash

#
#  Script to delete cloud run revisions
#

set -e

services=(k33-backend k33-backend-canary-gateway k33-backend-gateway k33-web-gateway sendgrid-reverse-proxy site-migration-gateway)

for service in "${services[@]}"; do

  revisions=($(gcloud run revisions list --service "$service" --format="value(metadata.name)" | sort -r))

  revisions=("${revisions[@]:1}")

  for revision in "${revisions[@]}"; do
    echo Deleting revision: "$revision"
    gcloud run revisions delete "$revision" --async --quiet
  done

done