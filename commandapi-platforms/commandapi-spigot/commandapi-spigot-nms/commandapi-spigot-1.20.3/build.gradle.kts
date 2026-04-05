plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.20.3"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1204)
	testImplementation(libs.org.spigotmc.spigot.v1204)

	compileOnly(project(":commandapi-bukkit-1.20.3"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-spigot-core"))
}