plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.4"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1214)
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1214)
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1214)
}

java {
	withSourcesJar()
	withJavadocJar()
}