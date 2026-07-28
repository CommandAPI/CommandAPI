plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support Spigot-mapped shade library"

dependencies {
	// `vh` must be before `core` so we resolve the correct version of `CommandAPIVersionHandler`
	api(project(":commandapi-spigot-vh"))
	api(project(":commandapi-spigot-core"))
	api(project(":commandapi-spigot-nms-dependency"))
}