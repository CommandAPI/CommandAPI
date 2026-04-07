plugins {
	id("buildlogic.java-conventions")
}

description = "Paper API support"

dependencies {
	compileOnly(paper.version.v2611)

	api(project(":commandapi-paper-core"))
}