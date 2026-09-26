plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for shared 26.x"

dependencies {
	compileOnly(spigot.version.v263)

	compileOnly(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}