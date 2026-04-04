plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "Paper support for 26.1"

dependencies {
	paperweight.paperDevBundle("26.1.1.build.+")
	compileOnly(libs.dev.jorel.commandapi.bukkit.v261)
	implementation(libs.dev.jorel.commandapi.paper.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}