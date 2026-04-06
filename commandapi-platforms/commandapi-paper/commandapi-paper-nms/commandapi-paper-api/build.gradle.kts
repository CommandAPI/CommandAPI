plugins {
	id("buildlogic.java-conventions")
}

description = "Paper API support"

dependencies {
	compileOnly(libs.io.papermc.paper.paper.api.v2611)

	api(project(":commandapi-paper-core"))
}