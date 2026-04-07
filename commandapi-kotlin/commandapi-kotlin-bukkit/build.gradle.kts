import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
	id("buildlogic.java-conventions")
	kotlin("jvm")
}

description = "Bukkit support in Kotlin"

dependencies {
	compileOnly(libs.paper.version.v1201)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)

	implementation(project(":commandapi-kotlin-core"))
	compileOnly(project(":commandapi-bukkit-core"))
}

kotlin {
	jvmToolchain(17)
}