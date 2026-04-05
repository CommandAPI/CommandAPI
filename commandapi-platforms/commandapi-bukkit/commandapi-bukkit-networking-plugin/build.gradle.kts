plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support Velocity networking plugin"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.api)

	compileOnly(project(":commandapi-core"))
}