plugins {
	id("buildlogic.java-conventions")
	kotlin("jvm")
}

description = "Bukkit support in Kotlin core library"

dependencies {
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)

	compileOnly(project(":commandapi-core"))
}

kotlin {
	jvmToolchain(17)
}