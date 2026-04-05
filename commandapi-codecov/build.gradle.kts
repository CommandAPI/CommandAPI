plugins {
	id("buildlogic.java-conventions")
}

description = "CommandAPI - Code Coverage Utility"

dependencies {
	implementation(project(":commandapi-core"))
	implementation(project(":commandapi-paper-core"))
	implementation(project(":commandapi-bukkit-test-toolkit"))
	implementation(project(":commandapi-spigot-core"))
	implementation(project(":commandapi-spigot-test-toolkit"))
	implementation(project(":commandapi-bukkit-core"))
	implementation(project(":commandapi-paper-test-toolkit"))
}