plugins {
	id("buildlogic.java-conventions")
	kotlin("jvm")
}

description = "Velocity support in Kotlin"

dependencies {
	compileOnly(paper.velocity.api)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)

	implementation(project(":commandapi-kotlin-core"))
	compileOnly(project(":commandapi-velocity-core"))
}

kotlin {
	jvmToolchain(17)
}