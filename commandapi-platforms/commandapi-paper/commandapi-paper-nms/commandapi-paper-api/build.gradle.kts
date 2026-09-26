plugins {
	id("buildlogic.java-conventions")
}

description = "Paper API support"

dependencies {
	compileOnly(paper.version.v263)

	api(project(":commandapi-paper-core"))
}