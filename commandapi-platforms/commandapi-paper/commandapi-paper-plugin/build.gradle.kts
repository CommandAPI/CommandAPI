plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support plugin"

dependencies {
	compileOnly(libs.paper.version.common)

	compileOnly(project(":commandapi-paper-shade"))
	compileOnly(project(":commandapi-bukkit-plugin-common"))
}