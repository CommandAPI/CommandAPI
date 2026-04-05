plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "Paper support for 1.21.11"

dependencies {
	paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")

	compileOnly(project(":commandapi-bukkit-1.21.11"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-paper-core"))
}