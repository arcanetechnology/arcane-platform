# OpenAPI spec for ESPv2

## Files

| File          | App      | User | Admin |
|---------------|----------|------|-------|
| `misc`        | All      | ☑️   | ☑️    |
| `platform`    | All      | ☑️   | ❌    |
| `invest`      | `invest` | ☑️   | ❌    |
| `admin`       | All      | ❌️   | ☑️    |
| `trade-admin` | `trade`  | ❌️   | ☑️    |
| `webhook`     | N/A      | ❌️   | ❌️️    |

## Folders

| Folders  | Hostname               | Used for                                | Environment                       | GCP projects   |
|----------|------------------------|-----------------------------------------|-----------------------------------|----------------|
| `test`   | `test.api.arcane.no`   | `docker-compose` based Acceptance tests | Developer machine & GitHub Action | `dev`          |
| `canary` | `canary.api.arcane.no` | `canary` deployment                     | `GCP`                             | `prod`         |
| `api`    | `[dev.]api.arcane.no`  | `api` for main deployment               | `GCP`                             | `dev` & `prod` |