# Azure CLI commands to setup login with Microsoft

## Azure hierarchy
1. Subscription
2. Tenant
3. Application

## CLI commands
 * Install `az` cli tool  
   https://learn.microsoft.com/en-us/cli/azure/install-azure-cli
 * Login  
   https://learn.microsoft.com/en-us/cli/azure/authenticate-azure-cli

```shell
az login --allow-no-subscriptions

az account tenant list
```

https://learn.microsoft.com/en-us/cli/azure/ad/app?view=azure-cli-latest#az-ad-app-create
```shell
az ad app create --display-name "K33 Dev" \
  --enable-access-token-issuance true \
  --enable-id-token-issuance true \
  --identifier-uris api://dev.auth.k33.com \
  --is-fallback-public-client false \
  --key-display-name "App Secret" \
  --key-type Password \
  --key-value $key \
  --sign-in-audience AzureADMyOrg \
  --web-home-page-url https://dev.k33.com \
  --web-redirect-uris https://dev.auth.k33.com/__/auth/handler

az ad app create --display-name "K33" \
  --enable-access-token-issuance true \
  --enable-id-token-issuance true \
  --identifier-uris api://auth.k33.com \
  --is-fallback-public-client false \
  --key-display-name "App Secret" \
  --key-type Password \
  --key-value ${AZURE_APP_SECRET} \
  --sign-in-audience AzureADandPersonalMicrosoftAccount \
  --web-home-page-url https://k33.com \
  --web-redirect-uris https://auth.k33.com/__/auth/handler
```

View
```shell
az ad app list --output table
az ad app owner list --id ${AZURE_APP_ID} --output table
az ad app credential list --id ${AZURE_APP_ID} --output table
az ad user list --output table
```