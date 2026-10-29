./gradlew clean build
./gradlew publishToMavenCentral
./gradlew modrinth -Ppublish-modrinth=true --no-configuration-cache
./gradlew publishPluginPublicationToHangar
./gradlew publishToGitHub