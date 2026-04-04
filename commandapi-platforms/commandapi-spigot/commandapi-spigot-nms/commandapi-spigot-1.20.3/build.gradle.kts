plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.20.3"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1203)
	compileOnly(libs.org.spigotmc.spigot.v1204)
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1204)
}

java {
	withSourcesJar()
	withJavadocJar()
}