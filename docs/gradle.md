# Common Gradle commands

## Check for new version of dependencies

    ./gradlew dependencyUpdates -Drevision=release

## Checking for dependency resolution

    ./gradlew :apps:arcane-platform-app:dependencyInsight --configuration runtimeClasspath --dependency dependency-name