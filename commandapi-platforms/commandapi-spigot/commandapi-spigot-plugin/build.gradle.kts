plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support Spigot-mapped plugin"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.api)

	compileOnly(project(":commandapi-spigot-shade"))
	compileOnly(project(":commandapi-bukkit-plugin-common"))
}