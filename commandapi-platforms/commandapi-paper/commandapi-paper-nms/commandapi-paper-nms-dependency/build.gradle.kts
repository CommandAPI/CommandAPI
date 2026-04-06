plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support NMS library"

dependencies {
	implementation(project(":commandapi-bukkit-nms-common"))

	api(project(":commandapi-paper-api"))

	api(project(":commandapi-paper-26.1", "include"))
	api(project(":commandapi-paper-1.21.11", "include"))
	api(project(":commandapi-paper-1.21.9", "include"))
	api(project(":commandapi-paper-1.21.6", "include"))
	api(project(":commandapi-paper-1.21.5", "include"))
	api(project(":commandapi-paper-1.21.4", "include"))
	api(project(":commandapi-paper-1.21.2", "include"))
	api(project(":commandapi-paper-1.21", "include"))
	api(project(":commandapi-paper-1.20.5", "include"))

	api(project(":commandapi-bukkit-26.1"))
	api(project(":commandapi-bukkit-1.21.11"))
	api(project(":commandapi-bukkit-1.21.9"))
	api(project(":commandapi-bukkit-1.21.6"))
	api(project(":commandapi-bukkit-1.21.5"))
	api(project(":commandapi-bukkit-1.21.4"))
	api(project(":commandapi-bukkit-1.21.2"))
	api(project(":commandapi-bukkit-1.21"))
	api(project(":commandapi-bukkit-1.20.5"))
}