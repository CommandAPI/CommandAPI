plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 26.1"

dependencies {
	compileOnly(spigot.version.v261)

	compileOnly(project(":commandapi-bukkit-26.1"))
	compileOnly(project(":commandapi-bukkit-26-common"))
	compileOnly(project(":commandapi-spigot-26-common"))
	api(project(":commandapi-spigot-core"))
}