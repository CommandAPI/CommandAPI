plugins {
	id("buildlogic.java-conventions")
}

description = "Annotations Library"

dependencies {
	compileOnly(libs.com.google.auto.service.auto.service)
	compileOnly(libs.org.spigotmc.spigot.api)
	compileOnly(libs.dev.jorel.commandapi.bukkit.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}