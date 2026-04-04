plugins {
	id("buildlogic.java-conventions")
}

description = "CommandAPI - Velocity support core library"

dependencies {
	implementation(libs.dev.jorel.commandapi.core)
	compileOnly(libs.org.jetbrains.annotations)
	compileOnly(libs.com.velocitypowered.velocity.api)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.com.mojang.authlib)
	compileOnly(libs.org.apache.logging.log4j.log4j.api)
	compileOnly(libs.dev.jorel.commandapi.preprocessor)
}

java {
	withSourcesJar()
	withJavadocJar()
}