plugins {
	id("buildlogic.java-conventions")
}

description = "Velocity support in Kotlin"

dependencies {
	compileOnly(libs.com.velocitypowered.velocity.api)
	implementation(libs.dev.jorel.commandapi.kotlin.core)
	compileOnly(libs.dev.jorel.commandapi.velocity.core)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)
}

java {
	withSourcesJar()
	withJavadocJar()
}