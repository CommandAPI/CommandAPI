plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.2"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1213)
	testImplementation(libs.org.spigotmc.spigot.v1213)

	compileOnly(project(":commandapi-bukkit-1.21.2"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-spigot-core"))
}