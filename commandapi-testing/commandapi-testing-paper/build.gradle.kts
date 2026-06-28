plugins {
	id("buildlogic.java-conventions")
}

description = "Testing plugin for Paper"

dependencies {
	compileOnly(paper.version.v1206)

	compileOnly(project(":commandapi-paper-shade"))
}