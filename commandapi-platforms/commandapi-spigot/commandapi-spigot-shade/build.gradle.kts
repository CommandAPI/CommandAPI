import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
	id("com.gradleup.shadow")
}

description = "Spigot support Spigot-mapped shade library"

dependencies {
	// `vh` must be before `core` so we resolve the correct version of `CommandAPIVersionHandler`
	shadow(project(":commandapi-spigot-vh"))
	shadow(project(":commandapi-spigot-core"))
	shadow(project(":commandapi-spigot-nms-dependency"))
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
}

afterEvaluate {
	configurations["shadowRuntimeElements"].isCanBeConsumed = false;
	configurations["shadow"].isCanBeConsumed = false
}