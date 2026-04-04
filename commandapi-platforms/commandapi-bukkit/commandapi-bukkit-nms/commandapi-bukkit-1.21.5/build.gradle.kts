plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.21.5"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1215)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1215)
	compileOnly(libs.io.papermc.paper.paper.api.v1214)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}