plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support Spigot-mapped shade library"

dependencies {
	api(project(":commandapi-spigot-core"))
	api(project(":commandapi-spigot-vh"))
	api(project(":commandapi-spigot-nms-dependency"))
}