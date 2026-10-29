plugins {
	id("buildlogic.java-conventions")
}

description = "CommandAPI - Spigot support test library implementation for 26.3"

dependencies {
	// Since 26.1, Minecraft isn't obfuscated anymore, so there's no Spigot/Mojang mapped variant to choose from
	compileOnly(spigot.version.v263)
	compileOnly(libs.com.mojang.brigadier)

	compileOnly(project(":commandapi-spigot-core"))
	compileOnly(project(":commandapi-spigot-26-common"))
	compileOnly(project(":commandapi-spigot-26.3"))
	compileOnly(project(":commandapi-bukkit-26-common"))
	compileOnly(project(":commandapi-bukkit-26.3"))
	compileOnly(project(":commandapi-bukkit-test-impl-26.3"))
}
