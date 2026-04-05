plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21"

dependencies {
	testImplementation(libs.org.spigotmc.spigot.v1211)
	compileOnly(libs.org.spigotmc.spigot.v1211)

	compileOnly(project(":commandapi-bukkit-1.21"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-spigot-core"))
}