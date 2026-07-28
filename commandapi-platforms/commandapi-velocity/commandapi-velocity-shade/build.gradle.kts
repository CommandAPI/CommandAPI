plugins {
	id("buildlogic.java-conventions")
}

description = "Velocity support shade library"

dependencies {
	implementation(project(":commandapi-velocity-core"))
}