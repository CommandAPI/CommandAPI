plugins {
	id("buildlogic.java-conventions")
}

description = "Core library"

dependencies {
	compileOnly(libs.com.google.guava.guava)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.com.mojang.authlib)
	compileOnly(libs.org.slf4j.slf4j.api)
	compileOnly(libs.org.apache.logging.log4j.log4j.api)
	compileOnly(libs.org.jetbrains.annotations)

	compileOnly(project(":commandapi-preprocessor"))
}