plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot Annotations"

dependencies {
	compileOnly(libs.com.google.auto.service.auto.service)
	compileOnly(libs.org.spigotmc.spigot.api)

	compileOnly(project(":commandapi-annotations"))
	compileOnly(project(":commandapi-spigot-core"))
}