plugins {
	id("buildlogic.java-conventions")
}

description = "CommandAPI - Velocity support plugin"

dependencies {
	implementation(libs.dev.jorel.commandapi.core)
	implementation(libs.dev.jorel.commandapi.plugin)
	compileOnly(libs.com.velocitypowered.velocity.api)
	implementation(libs.dev.jorel.commandapi.velocity.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}