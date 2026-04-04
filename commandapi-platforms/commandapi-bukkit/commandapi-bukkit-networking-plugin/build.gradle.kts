plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support Velocity networking plugin"

dependencies {
	implementation(libs.dev.jorel.commandapi.core)
	compileOnly(libs.org.spigotmc.spigot.api)
}

java {
	withSourcesJar()
	withJavadocJar()
}