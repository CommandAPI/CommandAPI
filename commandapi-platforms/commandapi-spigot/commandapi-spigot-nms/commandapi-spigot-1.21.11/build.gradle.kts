plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.11"

dependencies {
	testImplementation(libs.org.spigotmc.spigot.v12111)
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	compileOnly(libs.dev.jorel.commandapi.bukkit.v12111)
	compileOnly(libs.org.spigotmc.spigot.v12111)
}

java {
	withSourcesJar()
	withJavadocJar()
}