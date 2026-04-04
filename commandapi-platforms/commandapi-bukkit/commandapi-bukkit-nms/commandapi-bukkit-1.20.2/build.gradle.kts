plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.20.2"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1202)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	compileOnly(libs.io.papermc.paper.paper.api.v1202)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
	testImplementation(libs.org.spigotmc.spigot.v1202)
}

java {
	withSourcesJar()
	withJavadocJar()
}