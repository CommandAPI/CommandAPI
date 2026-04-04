plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.9"

dependencies {
	testImplementation(libs.org.spigotmc.spigot.v1219)
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.org.spigotmc.spigot.v1219)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1219)
}

java {
	withSourcesJar()
	withJavadocJar()
}