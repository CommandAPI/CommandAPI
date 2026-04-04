plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support for 1.21.4"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1214)
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.ca.bkaw.paper.nms.v1214)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
}

java {
	withSourcesJar()
	withJavadocJar()
}