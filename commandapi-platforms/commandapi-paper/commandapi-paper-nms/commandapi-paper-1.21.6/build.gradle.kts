plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support for 1.21.6"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1216)
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	compileOnly(libs.ca.bkaw.paper.nms.v1216)
}

java {
	withSourcesJar()
	withJavadocJar()
}