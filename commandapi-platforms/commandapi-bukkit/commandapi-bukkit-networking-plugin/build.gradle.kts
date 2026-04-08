plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support Velocity networking plugin"

dependencies {
	compileOnly(spigot.version.api)

	compileOnly(project(":commandapi-core"))
}