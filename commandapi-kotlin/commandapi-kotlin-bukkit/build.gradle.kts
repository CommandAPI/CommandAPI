plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support in Kotlin"

dependencies {
	implementation(libs.dev.jorel.commandapi.kotlin.core)
	compileOnly(libs.paper.version.v1201)
	compileOnly(libs.dev.jorel.commandapi.bukkit.core)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)
}

java {
	withSourcesJar()
	withJavadocJar()
}