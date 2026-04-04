plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.21.9"

dependencies {
	testImplementation(libs.org.spigotmc.spigot.v1219)
	compileOnly(libs.org.spigotmc.spigot.v1219)
	compileOnly(libs.io.papermc.paper.paper.api.v1219)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}