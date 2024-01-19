#!/bin/sh
echo "Old version: "
read -r oldVer
echo "New version: "
read -r newVer

##########################
# Maven example projects #
##########################

##########
# Bukkit #
##########

sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/automated-tests/README.md
sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/automated-tests/pom.xml

sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/automated-tests-shaded/README.md
sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/automated-tests-shaded/pom.xml

# commandtrees README dose not reference a dependency version
sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/commandtrees/pom.xml

sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/kotlindsl/README.md
sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/kotlindsl/pom.xml

sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/maven/README.md
sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/maven/pom.xml

sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/maven-annotations/README.md
sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/maven-annotations/pom.xml

sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/maven-shaded/README.md
sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/maven-shaded/pom.xml

sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/maven-shaded-annotations/README.md
sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/maven-shaded-annotations/pom.xml

# maven-shaded-tests README dose not reference a dependency version
sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/bukkit/maven-shaded-tests/pom.xml

############
# Velocity #
############

sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/velocity/maven/README.md
sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/velocity/maven/pom.xml

sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/velocity/maven-shaded/README.md
sed -i "s/<version>$oldVer<\/version>/<version>$newVer<\/version>/" examples/velocity/maven-shaded/pom.xml

###########################
# Gradle example projects #
###########################

#########
# Paper #
#########

# We're using Paper as the example dependency for example projects
sed -i "s/dev\.jorel:commandapi-paper-plugin:$oldVer/dev\.jorel:commandapi-paper-plugin:$newVer/" examples/bukkit/gradle-groovy/README.md
sed -i "s/dev\.jorel:commandapi-paper-plugin:$oldVer/dev\.jorel:commandapi-paper-plugin:$newVer/" examples/bukkit/gradle-groovy/build.gradle

sed -i "s/dev\.jorel:commandapi-paper-plugin:$oldVer/dev\.jorel:commandapi-paper-plugin:$newVer/" examples/bukkit/gradle-kotlin/README.md
sed -i "s/dev\.jorel:commandapi-paper-plugin:$oldVer/dev\.jorel:commandapi-paper-plugin:$newVer/" examples/bukkit/gradle-kotlin/build.gradle.kts

sed -i "s/dev\.jorel:commandapi-paper-core:$oldVer/dev\.jorel:commandapi-paper-core:$newVer/" examples/bukkit/kotlindsl-gradle/README.md
sed -i "s/dev\.jorel:commandapi-paper-core:$oldVer/dev\.jorel:commandapi-paper-core:$newVer/" examples/bukkit/kotlindsl-gradle/build.gradle.kts
sed -i "s/dev\.jorel:commandapi-kotlin-paper:$oldVer/dev\.jorel:commandapi-kotlin-paper:$newVer/" examples/bukkit/kotlindsl-gradle/README.md
sed -i "s/dev\.jorel:commandapi-kotlin-paper:$oldVer/dev\.jorel:commandapi-kotlin-paper:$newVer/" examples/bukkit/kotlindsl-gradle/build.gradle.kts

############
# Velocity #
############

sed -i "s/dev\.jorel:commandapi-velocity-core:$oldVer/dev\.jorel:commandapi-velocity-core:$newVer/" examples/velocity/gradle-groovy/README.md
sed -i "s/dev\.jorel:commandapi-velocity-core:$oldVer/dev\.jorel:commandapi-velocity-core:$newVer/" examples/velocity/gradle-groovy/build.gradle

sed -i "s/dev\.jorel:commandapi-velocity-core:$oldVer/dev\.jorel:commandapi-velocity-core:$newVer/" examples/velocity/gradle-kotlin/README.md
sed -i "s/dev\.jorel:commandapi-velocity-core:$oldVer/dev\.jorel:commandapi-velocity-core:$newVer/" examples/velocity/gradle-kotlin/build.gradle.kts

##############################
# CommandAPI project pom.xml #
##############################

# Set version in pom.xml using Maven
mvn versions:set -DnewVersion=$newVer
mvn versions:commit

mvn versions:set -DnewVersion=$newVer -P Platform.Bukkit
mvn versions:commit -P Platform.Bukkit

mvn versions:set -DnewVersion=$newVer -P Platform.Velocity
mvn versions:commit -P Platform.Velocity