plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 26.2"

dependencies {
	compileOnly(spigot.version.v262)

	compileOnly(project(":commandapi-bukkit-26.2"))
	compileOnly(project(":commandapi-bukkit-26-common"))
	compileOnly(project(":commandapi-spigot-26-common"))
	api(project(":commandapi-spigot-core"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}