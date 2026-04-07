plugins {
	id("buildlogic.java-conventions")
	kotlin("jvm")
}

description = "Paper support in Kotlin"

dependencies {
	compileOnly(libs.paper.version.v1201)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)

	implementation(project(":commandapi-kotlin-bukkit"))
	compileOnly(project(":commandapi-paper-core"))
}

kotlin {
	jvmToolchain(17)
}