plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support in Kotlin core library"

dependencies {
	api(libs.org.jetbrains.kotlin.kotlin.stdlib)

	compileOnly(project(":commandapi-core"))
}