plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support NMS library"

dependencies {
	implementation(project(":commandapi-bukkit-nms-common"))

	api(project(":commandapi-paper-api"))

	api(project(":commandapi-paper-26.1", "shadow"))
	api(project(":commandapi-paper-1.21.11", "shadow"))
	api(project(":commandapi-paper-1.21.9", "shadow"))
	api(project(":commandapi-paper-1.21.6", "shadow"))
	api(project(":commandapi-paper-1.21.5", "shadow"))
	api(project(":commandapi-paper-1.21.4", "shadow"))
	api(project(":commandapi-paper-1.21.2", "shadow"))
	api(project(":commandapi-paper-1.21", "shadow"))
	api(project(":commandapi-paper-1.20.5", "shadow"))

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