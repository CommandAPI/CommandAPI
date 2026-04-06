plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 26.1"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v261)

	compileOnly(project(":commandapi-bukkit-26.1"))
	api(project(":commandapi-spigot-core"))
}