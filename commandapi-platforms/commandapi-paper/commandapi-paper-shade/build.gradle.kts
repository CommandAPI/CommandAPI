import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support shade library"

dependencies {
	api(project(":commandapi-paper-core"))
	api(project(":commandapi-paper-vh"))
	api(project(":commandapi-paper-nms-dependency"))
	api(project(":commandapi-paper-mojang-mapped"))
}

tasks.withType<ShadowJar> {
	relocate("org.bukkit.craftbukkit.v1_20_R4", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R1", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R2", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R3", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R4", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R5", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R6", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R7", "org.bukkit.craftbukkit")
}