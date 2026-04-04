plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "Paper support for 1.21.9"

dependencies {
	paperweight.paperDevBundle("1.21.9-R0.1-SNAPSHOT")
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.dev.jorel.commandapi.bukkit.nms.common)
	compileOnly(libs.dev.jorel.commandapi.bukkit.v1219)
}

java {
	withSourcesJar()
	withJavadocJar()
}