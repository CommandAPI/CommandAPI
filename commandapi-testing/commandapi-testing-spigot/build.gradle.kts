plugins {
	id("buildlogic.java-conventions")
}

description = "Testing plugin for Spigot"

dependencies {
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(spigot.version.api)

	compileOnly(project(":commandapi-spigot-shade"))
}