plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support core library"

dependencies {
	compileOnly(libs.org.jetbrains.annotations)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.org.spigotmc.spigot.api)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
	compileOnly(libs.dev.jorel.commandapi.preprocessor)
}

java {
	withSourcesJar()
	withJavadocJar()
}