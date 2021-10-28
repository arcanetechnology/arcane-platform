# Acceptance Tests

Run tests using gradle run

    ./gradlew :apps:acceptance-tests:run --args="--select-package=no.arcane.platform.tests"

Build Docker image

    ./gradlew :apps:acceptance-tests:jibDockerBuild