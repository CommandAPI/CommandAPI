plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support for 1.21"

dependencies {
	compileOnly(libs.io.papermc.paper.paper.api.v121)
	compileOnly(libs.ca.bkaw.paper.nms.v121)
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.v121)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
}

java {
	withSourcesJar()
	withJavadocJar()
}