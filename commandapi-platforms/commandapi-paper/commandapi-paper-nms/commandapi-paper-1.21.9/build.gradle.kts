plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support for 1.21.9"

dependencies {
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.io.papermc.paper.paper.api.v1219)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	compileOnly(libs.ca.bkaw.paper.nms.v1219)
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1219)
}

java {
	withSourcesJar()
	withJavadocJar()
}