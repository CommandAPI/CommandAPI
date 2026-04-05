plugins {
	id("buildlogic.java-conventions")
}

description = "Core library"

dependencies {
	compileOnly(libs.org.jetbrains.annotations)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.com.mojang.authlib)
	compileOnly(libs.org.apache.logging.log4j.log4j.api)

	compileOnly(project(":commandapi-preprocessor"))
}