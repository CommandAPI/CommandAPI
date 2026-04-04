plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support for 1.21.5"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1215)
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.ca.bkaw.paper.nms.v1215)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
}

java {
	withSourcesJar()
	withJavadocJar()
}