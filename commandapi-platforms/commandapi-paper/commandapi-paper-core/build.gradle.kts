plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support core library"

dependencies {
	compileOnly(libs.org.jetbrains.annotations)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(paper.version.common)

	api(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}