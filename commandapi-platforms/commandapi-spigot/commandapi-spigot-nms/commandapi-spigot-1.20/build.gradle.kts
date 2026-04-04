plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.20"

dependencies {
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.v120)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1201)
	compileOnly(libs.org.spigotmc.spigot.v1201)
}

java {
	withSourcesJar()
	withJavadocJar()
}