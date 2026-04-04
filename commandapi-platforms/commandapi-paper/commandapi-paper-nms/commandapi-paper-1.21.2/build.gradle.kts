plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support for 1.21.2"

dependencies {
	compileOnly(libs.ca.bkaw.paper.nms.v1213)
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1212)
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
}

java {
	withSourcesJar()
	withJavadocJar()
}