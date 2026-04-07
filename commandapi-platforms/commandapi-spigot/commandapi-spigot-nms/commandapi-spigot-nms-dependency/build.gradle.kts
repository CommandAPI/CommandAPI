plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support Spigot-mapped NMS dependency"

dependencies {
	implementation(project(":commandapi-bukkit-nms-common", "spigot"))

	api(project(":commandapi-spigot-26.1"))
	api(project(":commandapi-spigot-1.21.11", "remapped"))
	api(project(":commandapi-spigot-1.21.9", "remapped"))
	api(project(":commandapi-spigot-1.21.6", "remapped"))
	api(project(":commandapi-spigot-1.21.5", "remapped"))
	api(project(":commandapi-spigot-1.21.4", "remapped"))
	api(project(":commandapi-spigot-1.21.2", "remapped"))
	api(project(":commandapi-spigot-1.21", "remapped"))
	api(project(":commandapi-spigot-1.20.5", "remapped"))
	api(project(":commandapi-spigot-1.20.3", "remapped"))
	api(project(":commandapi-spigot-1.20.2", "remapped"))
	api(project(":commandapi-spigot-1.20", "remapped"))

	api(project(":commandapi-bukkit-26.1"))
	api(project(":commandapi-bukkit-1.21.11", "spigot"))
	api(project(":commandapi-bukkit-1.21.9", "spigot"))
	api(project(":commandapi-bukkit-1.21.6", "spigot"))
	api(project(":commandapi-bukkit-1.21.5", "spigot"))
	api(project(":commandapi-bukkit-1.21.4", "spigot"))
	api(project(":commandapi-bukkit-1.21.2", "spigot"))
	api(project(":commandapi-bukkit-1.21", "spigot"))
	api(project(":commandapi-bukkit-1.20.5", "spigot"))
	api(project(":commandapi-bukkit-1.20.3", "spigot"))
	api(project(":commandapi-bukkit-1.20.2", "spigot"))
	api(project(":commandapi-bukkit-1.20", "spigot"))
}