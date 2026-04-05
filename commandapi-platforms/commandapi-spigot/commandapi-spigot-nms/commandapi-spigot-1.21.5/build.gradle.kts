plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.5"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1215)
	testImplementation(libs.org.spigotmc.spigot.v1215)

	compileOnly(project(":commandapi-bukkit-1.21.5"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-spigot-core"))
}