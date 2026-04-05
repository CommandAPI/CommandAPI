plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.9"

dependencies {
	testImplementation(libs.org.spigotmc.spigot.v1219)
	compileOnly(libs.org.spigotmc.spigot.v1219)

	compileOnly(project(":commandapi-bukkit-1.21.9"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-spigot-core"))
}