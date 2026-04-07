plugins {
	id("buildlogic.java-conventions")
	kotlin("jvm")
}

description = "Spigot support in Kotlin"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.api)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)

	implementation(project(":commandapi-kotlin-bukkit"))
	compileOnly(project(":commandapi-spigot-core"))
}

kotlin {
	jvmToolchain(17)
}