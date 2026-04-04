plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support for 1.21.11"

dependencies {
	compileOnly(libs.ca.bkaw.paper.nms.v12111)
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	compileOnly(libs.dev.jorel.commandapi.bukkit.v12111)
	compileOnly(libs.io.papermc.paper.paper.api.v12111)
}

java {
	withSourcesJar()
	withJavadocJar()
}