import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
	id("com.gradleup.shadow")
}

description = "Paper support shade library"

dependencies {
	// `vh` must be before `core` so we resolve the correct version of `CommandAPIVersionHandler`
	shadow(project(":commandapi-paper-vh"))
	// `mojang-mapped` must be before `core` so we resolve the correct version of `MojangMappedVersionHandler`
	shadow(project(":commandapi-paper-mojang-mapped"))
	shadow(project(":commandapi-paper-core"))
	shadow(project(":commandapi-paper-nms-dependency"))
}

tasks.named("build") {
	dependsOn(tasks.shadowJar)
}

tasks.named("test") {
	dependsOn(tasks.shadowJar)
}

tasks.withType<Jar> {
	archiveClassifier = "thin"
}

tasks.withType<ShadowJar> {
	archiveClassifier = ""
	configurations = listOf(project.configurations["shadow"])

	relocate("org.bukkit.craftbukkit.v1_20_R4", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R1", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R2", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R3", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R4", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R5", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R6", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R7", "org.bukkit.craftbukkit")
}

afterEvaluate {
	configurations["shadowRuntimeElements"].isCanBeConsumed = false;
	configurations["shadow"].isCanBeConsumed = false
}