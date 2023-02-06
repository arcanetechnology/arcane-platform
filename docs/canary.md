# Canary Deployment

Deploy Canary ESPv2 (canary.api.k33.com)

```shell
./infra/gcp/deploy-canary-espv2.sh
```

Canary deploy k33-backend

* with no traffic via api.k33.com
* full traffic via canary.api.k33.com (Canary ESPv2)

```shell
./infra/gcp/canary-deploy.sh
```

Redirect partial traffic to canary

* with % traffic via api.k33.com
* full traffic via canary.api.k33.com (Canary ESPv2)

```shell
gcloud run services update-traffic k33-backend --to-tags=canary=10
```

Redirect full traffic to latest

```shell
gcloud run services update-traffic k33-backend --to-latest
```