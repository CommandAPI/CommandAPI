plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "Paper support for 1.20.6"

dependencies {
	paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1205)
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
}

java {
	withSourcesJar()
	withJavadocJar()
}