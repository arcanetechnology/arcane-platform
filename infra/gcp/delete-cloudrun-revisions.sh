#!/usr/bin/env bash

#
#  Script to delete cloud run revisions
#

set -e

services=(arcane-platform arcane-platform-canary-gateway arcane-platform-gateway arcane-web-proxy platform-ui research-ui sendgrid-reverse-proxy site-migration-gateway)

for service in "${services[@]}"; do

  revisions=($(gcloud run revisions list --service "$service" --format="value(metadata.name)" | sort -r))

  revisions=("${revisions[@]:1}")

  for revision in "${revisions[@]}"; do
    echo Deleting revision: "$revision"
    gcloud run revisions delete "$revision" --async --quiet
  done

done