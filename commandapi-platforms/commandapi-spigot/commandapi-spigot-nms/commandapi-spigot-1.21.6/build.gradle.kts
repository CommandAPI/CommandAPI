plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.6"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1216)
	compileOnly(libs.org.spigotmc.spigot.v1216)
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1216)
}

java {
	withSourcesJar()
	withJavadocJar()
}