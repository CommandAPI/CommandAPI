plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 26.1"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.bukkit.v261)
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.org.spigotmc.spigot.v261)
}

java {
	withSourcesJar()
	withJavadocJar()
}