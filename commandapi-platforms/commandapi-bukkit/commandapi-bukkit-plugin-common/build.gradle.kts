plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support plugin common library"

dependencies {
	compileOnly(paper.version.v1211)

	api(project(":commandapi-plugin"))
	compileOnly(project(":commandapi-bukkit-core"))
}