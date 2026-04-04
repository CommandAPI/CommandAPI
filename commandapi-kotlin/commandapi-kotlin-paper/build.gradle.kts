plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support in Kotlin"

dependencies {
	implementation(libs.dev.jorel.commandapi.kotlin.bukkit)
	compileOnly(libs.paper.version.v1201)
	compileOnly(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)
}

java {
	withSourcesJar()
	withJavadocJar()
}