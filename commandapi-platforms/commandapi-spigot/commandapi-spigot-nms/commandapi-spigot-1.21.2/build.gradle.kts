plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.2"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1213)
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1212)
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1213)
}

java {
	withSourcesJar()
	withJavadocJar()
}