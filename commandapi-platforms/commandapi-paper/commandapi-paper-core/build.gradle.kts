plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support core library"

dependencies {
	compileOnly(libs.org.jetbrains.annotations)
	compileOnly(libs.dev.folia.folia.api)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.paper.version.common)

	compileOnly(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}