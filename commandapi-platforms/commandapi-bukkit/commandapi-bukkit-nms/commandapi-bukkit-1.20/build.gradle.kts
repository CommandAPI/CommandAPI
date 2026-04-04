plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.20"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
	testImplementation(libs.org.spigotmc.spigot.v1201)
	compileOnly(libs.org.spigotmc.spigot.v1201)
	compileOnly(libs.io.papermc.paper.paper.api.v1201)
}

java {
	withSourcesJar()
	withJavadocJar()
}