plugins {
	id("buildlogic.java-conventions")
}

description = "CommandAPI - Bukkit support test library implementation for 1.20"

dependencies {
	implementation(libs.dev.jorel.commandapi.bukkit.shade)
	implementation(libs.org.mockito.mockito.core)
	implementation(libs.dev.jorel.commandapi.bukkit.test.impl)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.de.tr7zw.item.nbt.api)
	testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
	compileOnly(libs.org.spigotmc.spigot.v1201)
	implementation(libs.com.github.seeseemelk.MockBukkit.v120)
}