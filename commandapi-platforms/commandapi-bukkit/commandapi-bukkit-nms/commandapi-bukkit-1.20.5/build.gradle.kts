plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.20.5"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1206)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	compileOnly(libs.io.papermc.paper.paper.api.v1206)
	testImplementation(libs.org.spigotmc.spigot.v1206)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}