plugins {
	id("buildlogic.java-conventions")
}

description = "Velocity support plugin tests"

dependencies {
	compileOnly(project(":commandapi-velocity-shade"))
}