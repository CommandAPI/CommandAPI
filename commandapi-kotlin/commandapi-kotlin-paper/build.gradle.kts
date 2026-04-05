plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support in Kotlin"

dependencies {
	compileOnly(libs.paper.version.v1201)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)

	compileOnly(project(":commandapi-kotlin-bukkit"))
	compileOnly(project(":commandapi-paper-core"))
}