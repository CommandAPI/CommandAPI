plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.4"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1214)
	testImplementation(libs.org.spigotmc.spigot.v1214)

	compileOnly(project(":commandapi-bukkit-1.21.4"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-spigot-core"))
}