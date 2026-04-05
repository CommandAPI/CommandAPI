plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.11"

dependencies {
	testImplementation(libs.org.spigotmc.spigot.v12111)
	compileOnly(libs.org.spigotmc.spigot.v12111)

	compileOnly(project(":commandapi-bukkit-1.21.11"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-spigot-core"))
}