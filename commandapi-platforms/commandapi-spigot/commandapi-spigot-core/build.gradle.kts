plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support core library"

dependencies {
	compileOnly(libs.org.jetbrains.annotations)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.org.spigotmc.spigot.api)

	compileOnly(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}