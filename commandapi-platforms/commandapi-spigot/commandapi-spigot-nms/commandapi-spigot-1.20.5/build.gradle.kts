plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.20.5"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1206)
	testImplementation(libs.org.spigotmc.spigot.v1206)

	compileOnly(project(":commandapi-bukkit-1.20.5"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-spigot-core"))
}