plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.5"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1215)
	compileOnly(libs.org.spigotmc.spigot.v1215)
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1215)
}

java {
	withSourcesJar()
	withJavadocJar()
}