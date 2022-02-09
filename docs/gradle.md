# Common Gradle commands

## Check for new version of dependencies

    ./gradlew refreshVersions

## Checking for dependency resolution

    ./gradlew :apps:arcane-platform-app:dependencyInsight --configuration runtimeClasspath --dependency dependency-name