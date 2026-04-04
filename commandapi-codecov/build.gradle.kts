plugins {
	id("buildlogic.java-conventions")
}

description = "CommandAPI - Code Coverage Utility"

dependencies {
	implementation(libs.dev.jorel.commandapi.core)
	implementation(libs.dev.jorel.commandapi.paper.core)
	implementation(libs.dev.jorel.commandapi.bukkit.test.toolkit)
	implementation(libs.dev.jorel.commandapi.spigot.core)
	implementation(libs.dev.jorel.commandapi.spigot.test.toolkit)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
	implementation(libs.dev.jorel.commandapi.paper.test.toolkit)
}

java {
	withSourcesJar()
	withJavadocJar()
}