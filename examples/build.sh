#!/bin/bash

# Exit if any command fails
set -e

echo "Building Maven examples"

# Find all pom.xml files
for folder in $(find $PWD -name "pom.xml" -print0 | xargs -0 dirname); do
	# The parentheses are required to only change the directory for the command so the second find $PWD does not break
	(cd "$folder" && mvn clean verify)
done

echo "Building Gradle examples"

# Find all settings.gradle files
for folder in $(find $PWD -name "build.gradle*" -print0 | xargs -0 dirname); do
	# Gradle does not output the name of the project that it is currently building
	echo "$folder"
	# Add "mavenLocal()" repository just for this step in ci.
	# Generally in Gradle you should *never* use the mavenLocal() repository, but because we are doing this with
	# versions of the plugin which are not published on Maven Central this hack is necessary.
	(cd "$folder" && sed -i-backup -e "s/mavenCentral()/mavenCentral()\nmavenLocal()/g" build.gradle* && gradle clean jar)
done
