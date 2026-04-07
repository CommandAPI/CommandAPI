plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot Annotations"

dependencies {
	compileOnly(libs.com.google.auto.service.auto.service)
	compileOnly(spigot.version.api)

	implementation(project(":commandapi-bukkit-annotations"))
	compileOnly(project(":commandapi-spigot-core"))
}