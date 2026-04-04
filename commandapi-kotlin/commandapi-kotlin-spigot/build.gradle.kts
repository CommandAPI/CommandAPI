plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support in Kotlin"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.api)
	implementation(libs.dev.jorel.commandapi.kotlin.bukkit)
	compileOnly(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)
}

java {
	withSourcesJar()
	withJavadocJar()
}