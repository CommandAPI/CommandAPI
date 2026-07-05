plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for shared 26.1 and 26.2"

dependencies {
	compileOnly(spigot.version.v262)

	compileOnly(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}