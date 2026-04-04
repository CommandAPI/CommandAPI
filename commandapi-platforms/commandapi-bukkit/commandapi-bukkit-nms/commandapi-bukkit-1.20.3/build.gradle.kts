plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.20.3"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1204)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1204)
	compileOnly(libs.io.papermc.paper.paper.api.v1204)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}