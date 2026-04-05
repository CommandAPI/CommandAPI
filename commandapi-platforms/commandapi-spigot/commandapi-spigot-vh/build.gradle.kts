plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support version handler"

dependencies {
	compileOnly(libs.spigot.version.common)

	compileOnly(project(":commandapi-spigot-nms-dependency"))
}