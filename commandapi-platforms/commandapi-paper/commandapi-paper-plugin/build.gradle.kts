plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support plugin"

dependencies {
	implementation(libs.dev.jorel.commandapi.paper.shade)
	implementation(libs.dev.jorel.commandapi.bukkit.plugin.common)
	compileOnly(libs.paper.version.common)
}

java {
	withSourcesJar()
	withJavadocJar()
}