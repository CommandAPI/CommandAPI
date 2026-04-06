plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support Spigot-mapped NMS dependency"

dependencies {
	implementation(project(":commandapi-bukkit-nms-common"))

	api(project(":commandapi-spigot-26.1"))
	api(project(":commandapi-spigot-1.21.11"))
	api(project(":commandapi-spigot-1.21.9"))
	api(project(":commandapi-spigot-1.21.6"))
	api(project(":commandapi-spigot-1.21.5"))
	api(project(":commandapi-spigot-1.21.4"))
	api(project(":commandapi-spigot-1.21.2"))
	api(project(":commandapi-spigot-1.21"))
	api(project(":commandapi-spigot-1.20.5"))
	api(project(":commandapi-spigot-1.20.3"))
	api(project(":commandapi-spigot-1.20.2"))
	api(project(":commandapi-spigot-1.20"))

	api(project(":commandapi-bukkit-26.1"))
	api(project(":commandapi-bukkit-1.21.11"))
	api(project(":commandapi-bukkit-1.21.9"))
	api(project(":commandapi-bukkit-1.21.6"))
	api(project(":commandapi-bukkit-1.21.5"))
	api(project(":commandapi-bukkit-1.21.4"))
	api(project(":commandapi-bukkit-1.21.2"))
	api(project(":commandapi-bukkit-1.21"))
	api(project(":commandapi-bukkit-1.20.5"))
	api(project(":commandapi-bukkit-1.20.3"))
	api(project(":commandapi-bukkit-1.20.2"))
	api(project(":commandapi-bukkit-1.20"))
}