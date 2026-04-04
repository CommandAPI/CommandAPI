plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.21.6"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1216)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1216)
	compileOnly(libs.io.papermc.paper.paper.api.v1216)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}