plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot Annotations"

dependencies {
	compileOnly(libs.com.google.auto.service.auto.service)
	implementation(libs.dev.jorel.commandapi.spigot.core)
	compileOnly(libs.org.spigotmc.spigot.api)
	implementation(libs.dev.jorel.commandapi.annotations)
}

java {
	withSourcesJar()
	withJavadocJar()
}