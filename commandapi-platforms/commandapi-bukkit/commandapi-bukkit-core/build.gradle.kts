plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support core library"

dependencies {
	implementation(libs.dev.jorel.commandapi.core)
	compileOnly(libs.dev.folia.folia.api)
	compileOnly(libs.com.mojang.brigadier)
	testImplementation(libs.net.kyori.adventure.platform.bukkit)
	compileOnly(libs.com.mojang.authlib)
	compileOnly(libs.paper.version.v1201)
	compileOnly(libs.dev.jorel.commandapi.preprocessor)
}

java {
	withSourcesJar()
	withJavadocJar()
}