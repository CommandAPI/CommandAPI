plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit Annotations"

if (rootProject.findProject(":commandapi-bukkit-core") != null) {
	dependencies {
		compileOnly(libs.com.google.auto.service.auto.service)
		compileOnly(spigot.version.api)

		compileOnly(project(":commandapi-bukkit-core"))
	}
}