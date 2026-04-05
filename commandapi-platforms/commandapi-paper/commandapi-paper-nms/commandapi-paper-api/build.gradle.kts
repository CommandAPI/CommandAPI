plugins {
	id("buildlogic.java-conventions")
}

description = "Paper API support"

dependencies {
	compileOnly(libs.paper.version.v261)

	compileOnly(project(":commandapi-paper-core"))
}