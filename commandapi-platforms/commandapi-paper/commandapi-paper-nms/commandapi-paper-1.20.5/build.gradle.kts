plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "Paper support for 1.20.6"

dependencies {
	paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")

	compileOnly(project(":commandapi-bukkit-1.20.5"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-paper-core"))
}