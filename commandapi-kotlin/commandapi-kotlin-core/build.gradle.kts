plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support in Kotlin core library"

dependencies {
	compileOnly(libs.dev.jorel.commandapi.core)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)
}

java {
	withSourcesJar()
	withJavadocJar()
}