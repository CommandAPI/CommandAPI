plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support in Kotlin"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.api)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)

	compileOnly(project(":commandapi-kotlin-bukkit"))
	compileOnly(project(":commandapi-spigot-core"))
}