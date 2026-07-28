plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for shared 26.1 and 26.2"

dependencies {
	compileOnly(spigot.version.v262)

	compileOnly(project(":commandapi-bukkit-26-common"))
	api(project(":commandapi-spigot-core"))
}