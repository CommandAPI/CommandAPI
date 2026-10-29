import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
	id("com.gradleup.shadow")
}

description = "Bukkit support Velocity networking plugin"

dependencies {
	compileOnly(spigot.version.api)

	implementation(project(":commandapi-core"))
}

tasks.withType<ProcessResources> {
	val properties = mapOf(
		"version" to version,
	)
	inputs.properties(properties)
	filesMatching("plugin.yml") {
		expand(properties)
	}
}

tasks.withType<ShadowJar> {
	minimize()
}

val renameForPublishing = tasks.register<Copy>("renameForPublishing") {
	group = "publishing"
	description = "Copies the shadowJar output and renames the result for publishing to Modrinth"

	// Local, so the rename action doesn't reference the build script (not supported by the configuration cache)
	val jarName = "CommandAPI-$version-Networking-Plugin.jar"
	from(tasks.shadowJar)
	into(layout.buildDirectory.dir("publishing"))
	rename { jarName }
}

tasks.named("test") {
	dependsOn(tasks.shadowJar)
}

tasks.withType<Jar> {
	archiveClassifier = "thin"
}

tasks.withType<ShadowJar> {
	archiveClassifier = ""
}

afterEvaluate {
	configurations["shadowRuntimeElements"].isCanBeConsumed = false
	configurations["shadow"].isCanBeConsumed = false
}