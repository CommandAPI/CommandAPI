plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.21"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	testImplementation(libs.org.spigotmc.spigot.v1211)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
	compileOnly(libs.org.spigotmc.spigot.v1211)
	compileOnly(libs.io.papermc.paper.paper.api.v1211)
}

java {
	withSourcesJar()
	withJavadocJar()
}