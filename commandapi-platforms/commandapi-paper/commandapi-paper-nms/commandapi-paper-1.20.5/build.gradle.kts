plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support for 1.20.6"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1205)
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	compileOnly(libs.ca.bkaw.paper.nms.v1206)
	compileOnly(libs.io.papermc.paper.paper.api.v1206)
}

java {
	withSourcesJar()
	withJavadocJar()
}