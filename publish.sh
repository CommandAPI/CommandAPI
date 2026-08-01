./gradlew clean build
./gradlew publishToMavenCentral
./gradlew modrinth -Ppublish-modrinth=true --no-configuration-cache
./gradlew publishPluginPublicationToHangar --no-configuration-cache
./gradlew publishToGitHub --no-configuration-cache