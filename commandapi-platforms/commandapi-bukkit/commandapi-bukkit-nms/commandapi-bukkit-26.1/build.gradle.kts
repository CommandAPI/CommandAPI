plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 26.1"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v261)

	compileOnly(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}