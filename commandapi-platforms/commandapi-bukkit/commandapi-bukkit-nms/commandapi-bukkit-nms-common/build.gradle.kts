plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support NMS common library"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.api)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
	compileOnly(libs.paper.version.bukkit.common)
}

java {
	withSourcesJar()
	withJavadocJar()
}