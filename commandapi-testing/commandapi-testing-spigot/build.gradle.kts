plugins {
	id("buildlogic.java-conventions")
}

description = "Testing plugin for Spigot"

dependencies {
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.org.spigotmc.spigot.api)
	implementation(libs.dev.jorel.commandapi.spigot.shade)
}

java {
	withSourcesJar()
	withJavadocJar()
}