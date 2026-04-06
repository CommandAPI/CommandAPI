plugins {
	id("buildlogic.java-conventions")
}

description = "Velocity support plugin"

dependencies {
	compileOnly(libs.com.velocitypowered.velocity.api)

	implementation(project(":commandapi-velocity-core"))
	implementation(project(":commandapi-plugin"))
}