plugins {
	id("buildlogic.java-conventions")
}

description = "Velocity support plugin"

dependencies {
	compileOnly(paper.velocity.api)

	implementation(project(":commandapi-velocity-core"))
	implementation(project(":commandapi-plugin"))
}