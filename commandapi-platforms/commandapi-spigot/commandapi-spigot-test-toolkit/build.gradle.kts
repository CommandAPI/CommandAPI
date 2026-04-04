plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support testing toolkit"

dependencies {
	compileOnly(libs.paper.version.v1218)
	implementation(libs.dev.jorel.commandapi.bukkit.test.toolkit)
	implementation(libs.org.mockbukkit.mockbukkit.mockbukkit.v121)
	compileOnly(libs.dev.jorel.commandapi.spigot.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}