plugins {
	id("buildlogic.java-conventions")
}

description = "Testing plugin for Paper"

dependencies {
	compileOnly(libs.paper.version.v1206)

	compileOnly(project(":commandapi-paper-shade"))
}