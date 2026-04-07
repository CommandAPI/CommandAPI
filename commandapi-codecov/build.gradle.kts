plugins {
	id("buildlogic.java-conventions")
}

description = "CommandAPI - Code Coverage Utility"

if (rootProject.findProject(":commandapi-paper-core") != null) {
	dependencies {
		implementation(project(":commandapi-core"))
		implementation(project(":commandapi-bukkit-core"))
		implementation(project(":commandapi-paper-core"))
		implementation(project(":commandapi-bukkit-test-toolkit"))
		implementation(project(":commandapi-paper-test-toolkit"))
	}
}

if (rootProject.findProject(":commandapi-spigot-core") != null) {
	dependencies {
		implementation(project(":commandapi-core"))
		implementation(project(":commandapi-bukkit-core"))
		implementation(project(":commandapi-spigot-core"))
		implementation(project(":commandapi-bukkit-test-toolkit"))
		implementation(project(":commandapi-spigot-test-toolkit"))
	}
}