plugins {
	id("buildlogic.java-conventions")
}

description = "Velocity support plugin"

dependencies {
	compileOnly(libs.com.velocitypowered.velocity.api)

	compileOnly(project(":commandapi-velocity-core"))
	compileOnly(project(":commandapi-plugin"))
}