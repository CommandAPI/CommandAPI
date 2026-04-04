plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.20.2"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1202)
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1202)
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1202)
}

java {
	withSourcesJar()
	withJavadocJar()
}