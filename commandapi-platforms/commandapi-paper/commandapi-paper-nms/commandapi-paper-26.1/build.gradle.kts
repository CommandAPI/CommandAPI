plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support for 26.1"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.bukkit.v261)
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.io.papermc.paper.paper.api.v2611)
	compileOnly(libs.ca.bkaw.paper.nms.v261)
}

java {
	withSourcesJar()
	withJavadocJar()
}