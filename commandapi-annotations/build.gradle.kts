plugins {
	id("buildlogic.java-conventions")
}

description = "Annotations Library"

dependencies {
	compileOnly(libs.com.google.auto.service.auto.service)
	compileOnly(libs.org.spigotmc.spigot.api)

	compileOnly(project(":commandapi-bukkit-core"))
}