plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support in Kotlin"

dependencies {
	compileOnly(libs.paper.version.v1201)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)

	compileOnly(project(":commandapi-kotlin-core"))
	compileOnly(project(":commandapi-bukkit-core"))
}