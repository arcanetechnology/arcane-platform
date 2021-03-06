# Infra setup

## Install and initialize `gcloud`  

Ref: https://cloud.google.com/sdk/docs/quickstart  
Ref: https://cloud.google.com/sdk/docs/initializing  
```shell
gcloud components update

gcloud auth login
gcloud config set project $GCP_PROJECT_ID
gcloud config set compute/region europe-west1
gcloud config set compute/zone europe-west1-b
gcloud config set run/region europe-west1
# or
gcloud init
```

Output of `gcloud config list`
```text
[compute]
region = europe-west1
zone = europe-west1-b
[core]
account = user@arcane.no
disable_usage_reporting = True
project = <<GCP_PROJECT_ID>
[run]
region = europe-west1

Your active configuration is: [default]
```

## Add docker repository to GCP Artifact Registry

```shell
gcloud artifacts repositories list
gcloud artifacts repositories create platform --location=europe --repository-format=DOCKER
```

## Run backend app

Ref: https://cloud.google.com/run/docs/quickstarts/prebuilt-deploy  

See: [deploy.sh](deploy.sh)

* Identity Platform with Cloud Run
Ref: https://cloud.google.com/run/docs/tutorials/identity-platform

## Run ESP

* Deploy endpoints configuration  
Update `x-google-backend/address` and `${GCP_PROJECT_ID}` in
`libs/clients/arcane-platform-client/src/main/openapi/arcane-platform-api.yaml`
with `URL` of backend app from previous step.
```shell
gcloud endpoints services deploy libs/clients/arcane-platform-client/src/main/openapi/arcane-platform-api.yaml
```

* Build esp v2 docker image for cloud run 
Ref: https://cloud.google.com/endpoints/docs/openapi/get-started-cloud-run  
See: [deploy-espv2.sh](deploy-espv2.sh)  

* Run esp

* Map `api.arcane.no` domain to esp  
Ref: https://cloud.google.com/run/docs/mapping-custom-domains

## Restrict access to the arcane-platform-app only via ESP  

Ref: https://cloud.google.com/run/docs/tutorials/secure-services  

Create service accounts for both the cloud run services.
```shell
gcloud iam service-accounts create arcane-platform \
    --description="Service Account for arcane-platform cloud run service" \
    --display-name="arcane-platform"

gcloud iam service-accounts create arcane-platform-gateway \
    --description="Service Account for arcane-platform-gateway cloud run service" \
    --display-name="arcane-platform-gateway"
```

Pass their names in `--service-account` options for `gcloud run deploy`:
* `--service-account arcane-platform-gateway` for `arcane-platform-gateway`  
* `--service-account arcane-platform` for `arcane-platform`

```shell
gcloud run services add-iam-policy-binding arcane-platform \
  --member serviceAccount:arcane-platform-gateway@"$GCP_PROJECT_ID".iam.gserviceaccount.com \
  --role roles/run.invoker \
  --region='europe-west1'
```

Options for `gcloud run deploy`:  
* `--allow-unauthenticated` for `arcane-platform-gateway`  
* `--no-allow-unauthenticated` for `arcane-platform`

### Verification  
Direct access to `arcane-platform` (https://"$GCP_BACKEND_HOST"/ping) should be blocked.  
Access via esp `arcane-platform-gateway` (https://api.arcane.no/ping) should be allowed.

### Additional roles

Assign role to service accounts so that it can report monitoring metrics
```shell
gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member serviceAccount:arcane-platform-gateway@"$GCP_PROJECT_ID".iam.gserviceaccount.com \
  --role roles/monitoring.metricWriter

gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member serviceAccount:arcane-platform@"$GCP_PROJECT_ID".iam.gserviceaccount.com \
  --role roles/monitoring.metricWriter
```

Assign role to service accounts so that it can report traces
```shell
gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member serviceAccount:arcane-platform-gateway@"$GCP_PROJECT_ID".iam.gserviceaccount.com \
  --role roles/cloudtrace.agent

gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member serviceAccount:arcane-platform@"$GCP_PROJECT_ID".iam.gserviceaccount.com \
  --role roles/cloudtrace.agent
```

#### For arcane-platform-gateway
Assign role to service account so that it can report Endpoint stats
```shell
gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member serviceAccount:arcane-platform-gateway@"$GCP_PROJECT_ID".iam.gserviceaccount.com \
  --role roles/servicemanagement.serviceController
```

#### For arcane-platform
Assign role to service account so that it can access GCP Secret manager.
```shell
gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member serviceAccount:arcane-platform@"$GCP_PROJECT_ID".iam.gserviceaccount.com \
  --role roles/secretmanager.secretAccessor
```

Assign role to service account so that it can get and create users in Firebase Authentication / Customer IAM.
```shell
gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member serviceAccount:arcane-platform@"$GCP_PROJECT_ID".iam.gserviceaccount.com \
  --role roles/firebaseauth.admin
```

Assign role to service account so that it can access Firebase custom tokens.
```shell
gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member serviceAccount:arcane-platform@"$GCP_PROJECT_ID".iam.gserviceaccount.com \
  --role roles/iam.serviceAccountTokenCreator
```

Assign role to service account so that it can access Firestore database.
```shell
gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member serviceAccount:arcane-platform@"$GCP_PROJECT_ID".iam.gserviceaccount.com \
  --role roles/datastore.user
```

Assign role to service account so that it can access GCS.
```shell
gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member serviceAccount:arcane-platform@"$GCP_PROJECT_ID".iam.gserviceaccount.com \
  --role roles/storage.objectAdmin
```

## Cron jobs using GCP scheduler invoking Cloud Run

```shell
gcloud scheduler jobs delete update-firebase-users-stats-job \
  --location europe-west1

gcloud scheduler jobs create http update-firebase-users-stats-job \
  --location europe-west1 \
  --schedule "55 * * * *" \
  --uri=https://"$GCP_BACKEND_HOST"/admin/jobs/update-firebase-users-stats \
  --oidc-service-account-email=arcane-platform-gateway@"$GCP_PROJECT_ID".iam.gserviceaccount.com   \
  --oidc-token-audience=https://"$GCP_BACKEND_HOST"
```

## GCP Workload Identity Federation for GitHub Actions

Create workload id pool for GitHub Actions.
```shell
gcloud iam workload-identity-pools create "github-actions-workload-id-pool" \
  --project="${GCP_PROJECT_ID}" \
  --location="global" \
  --display-name="Github Actions workload ID pool"
```

Verify Pool ID
```shell
gcloud iam workload-identity-pools describe github-actions-workload-id-pool \
  --location="global" \
  --format="value(name)"
```

It should be of this format:

    projects/<GCP project id number>/locations/global/workloadIdentityPools/github-actions-workload-id-pool

Create ID provider for workload ID pool.
```shell
gcloud iam workload-identity-pools providers create-oidc "github-workload-id-provider" \
  --project="${GCP_PROJECT_ID}" \
  --location="global" \
  --workload-identity-pool="github-actions-workload-id-pool" \
  --display-name="Github workload ID provider" \
  --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.aud=assertion.aud,attribute.repository_owner=assertion.repository_owner,attribute.repository=assertion.repository" \
  --issuer-uri="https://token.actions.githubusercontent.com"
```

Allow authentication by GitHub workload ID providers to impersonate GCP service account.

```shell
GCP_PROJECT_NUMBER=$(gcloud projects describe "${GCP_PROJECT_ID}" --format="value(projectNumber)")

gcloud iam service-accounts add-iam-policy-binding "github@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
  --project="${GCP_PROJECT_ID}" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/projects/${GCP_PROJECT_NUMBER}/locations/global/workloadIdentityPools/github-actions-workload-id-pool/attribute.repository_owner/arcanetechnology"
```