plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 26.2"

dependencies {
	compileOnly(spigot.version.v262)

	compileOnly(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-bukkit-26-common"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}