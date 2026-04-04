plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "Paper support for 1.21.4"

dependencies {
	paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1214)
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
}

java {
	withSourcesJar()
	withJavadocJar()
}