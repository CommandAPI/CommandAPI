plugins {
	id("buildlogic.java-conventions")
}

description = "Annotations Library"

dependencies {
	compileOnly(libs.com.google.auto.service.auto.service)
	compileOnly(spigot.version.api)

	compileOnly(project(":commandapi-bukkit-core"))
}