plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21"

dependencies {
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.v121)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1211)
	compileOnly(libs.org.spigotmc.spigot.v1211)
}

java {
	withSourcesJar()
	withJavadocJar()
}