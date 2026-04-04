plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.21.11"

dependencies {
	testImplementation(libs.org.spigotmc.spigot.v12111)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	compileOnly(libs.org.spigotmc.spigot.v12111)
	compileOnly(libs.io.papermc.paper.paper.api.v12111)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}