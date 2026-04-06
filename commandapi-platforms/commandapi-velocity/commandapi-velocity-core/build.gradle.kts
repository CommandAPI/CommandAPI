plugins {
	id("buildlogic.java-conventions")
}

description = "CommandAPI - Velocity support core library"

dependencies {
	compileOnly(libs.org.jetbrains.annotations)
	compileOnly(libs.com.velocitypowered.velocity.api)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.com.mojang.authlib)
	compileOnly(libs.org.apache.logging.log4j.log4j.api)

	api(project(":commandapi-core"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}