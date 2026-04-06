plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support shade library"

dependencies {
	api(project(":commandapi-paper-core"))
	api(project(":commandapi-paper-vh"))
	api(project(":commandapi-paper-nms-dependency"))
	api(project(":commandapi-paper-mojang-mapped"))
}