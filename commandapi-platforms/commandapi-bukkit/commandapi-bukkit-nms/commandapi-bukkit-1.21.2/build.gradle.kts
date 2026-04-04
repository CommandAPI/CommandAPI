plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.21.2"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1213)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	compileOnly(libs.io.papermc.paper.paper.api.v1213)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
	testImplementation(libs.org.spigotmc.spigot.v1213)
}

java {
	withSourcesJar()
	withJavadocJar()
}