plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support Spigot-mapped plugin"

dependencies {
	implementation(libs.dev.jorel.commandapi.bukkit.plugin.common)
	compileOnly(libs.org.spigotmc.spigot.api)
	implementation(libs.dev.jorel.commandapi.spigot.shade)
}

java {
	withSourcesJar()
	withJavadocJar()
}