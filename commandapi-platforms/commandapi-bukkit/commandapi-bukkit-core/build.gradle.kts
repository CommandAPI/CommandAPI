plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support core library"

dependencies {
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.com.mojang.authlib)
	compileOnly(paper.version.v1201)

	api(project(":commandapi-core"))
	compileOnly(project(":commandapi-preprocessor"))
}