plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "Paper support for 1.21.2"

dependencies {
	paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")

	compileOnly(project(":commandapi-bukkit-1.21.2"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-paper-core"))
}