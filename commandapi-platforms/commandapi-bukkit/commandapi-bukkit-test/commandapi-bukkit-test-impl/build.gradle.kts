plugins {
	id("buildlogic.java-conventions")
}

description = "CommandAPI - Bukkit support test library implementation"

dependencies {
	implementation(libs.dev.jorel.commandapi.bukkit.shade)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.spigot.version.common)
	compileOnly(libs.paper.version.bukkit.common)
}

java {
	withSourcesJar()
	withJavadocJar()
}