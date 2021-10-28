# Locally test GitHub Actions

* Install `act` - https://github.com/nektos/act  

## Commands

CI workflow

    act pull_request

CD workflow

    act \
      -s GCP_PROJECT_ID=<gcp-project-id> \
      -s GCP_SA_KEY=$(cat <gcp-service-account-credentials-json-file> | base64)