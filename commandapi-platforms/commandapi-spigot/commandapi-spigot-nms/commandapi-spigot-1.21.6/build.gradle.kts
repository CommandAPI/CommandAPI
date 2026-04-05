plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.6"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1216)
	testImplementation(libs.org.spigotmc.spigot.v1216)

	compileOnly(project(":commandapi-bukkit-1.21.6"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-spigot-core"))
}