# Exit script if any tests fails so they can be investigated
set -e

runTest() {
	echo "Running tests for $1"
	mvn clean verify -Dmaven.javadoc.skip=true -P Platform.Bukkit -pl :commandapi-paper-test-tests -P $1
}

# Test all versions
# 1.20.5 & 1.20.6
runTest Minecraft_1_20_5
