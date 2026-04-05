plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support core library"

dependencies {
	compileOnly(libs.dev.folia.folia.api)
	compileOnly(libs.com.mojang.brigadier)
	testImplementation(libs.net.kyori.adventure.platform.bukkit)
	compileOnly(libs.com.mojang.authlib)
	compileOnly(libs.paper.version.v1201)

	api(project(":commandapi-core"))
	compileOnly(project(":commandapi-preprocessor"))
}