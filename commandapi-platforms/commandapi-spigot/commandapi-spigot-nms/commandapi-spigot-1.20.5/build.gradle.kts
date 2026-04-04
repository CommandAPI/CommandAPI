plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.20.5"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1205)
	compileOnly(libs.org.spigotmc.spigot.v1206)
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1206)
}

java {
	withSourcesJar()
	withJavadocJar()
}