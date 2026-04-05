plugins {
	id("buildlogic.java-conventions")
}

description = "Velocity support in Kotlin"

dependencies {
	compileOnly(libs.com.velocitypowered.velocity.api)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)

	compileOnly(project(":commandapi-kotlin-core"))
	compileOnly(project(":commandapi-velocity-core"))
}