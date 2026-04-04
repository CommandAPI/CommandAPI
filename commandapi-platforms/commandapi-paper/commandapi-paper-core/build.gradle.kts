plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support core library"

dependencies {
	compileOnly(libs.org.jetbrains.annotations)
	compileOnly(libs.dev.folia.folia.api)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.paper.version.common)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
	compileOnly(libs.dev.jorel.commandapi.preprocessor)
}

java {
	withSourcesJar()
	withJavadocJar()
}