# Canary Deployment

Deploy Canary ESPv2 (canary.api.arcane.no)

```shell
./infra/gcp/deploy-canary-espv2.sh
```

Canary deploy arcane-platform

* with no traffic via api.arcane.no
* full traffic via canary.api.arcane.no (Canary ESPv2)

```shell
./infra/gcp/canary-deploy.sh
```

Redirect partial traffic to canary

* with % traffic via api.arcane.no
* full traffic via canary.api.arcane.no (Canary ESPv2)

```shell
gcloud run services update-traffic arcane-platform --to-revisions=arcane-platform-<version>=10
```

Redirect full traffic to latest

```shell
gcloud run services update-traffic arcane-platform --to-latest
```