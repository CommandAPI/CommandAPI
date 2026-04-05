plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support plugin common library"

dependencies {
	compileOnly(libs.paper.version.v1211)

	compileOnly(project(":commandapi-plugin"))
	compileOnly(project(":commandapi-bukkit-core"))
}