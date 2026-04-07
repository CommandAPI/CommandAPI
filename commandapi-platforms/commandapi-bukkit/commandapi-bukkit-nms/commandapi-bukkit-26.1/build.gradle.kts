plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 26.1"

dependencies {
	compileOnly(spigot.version.v261)

	compileOnly(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}