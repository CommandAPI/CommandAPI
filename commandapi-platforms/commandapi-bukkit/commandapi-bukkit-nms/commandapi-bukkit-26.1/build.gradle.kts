plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 26.1"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v261)
	compileOnly(libs.io.papermc.paper.paper.api.v2611)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}