import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
	id("com.modrinth.minotaur")
	id("com.gradleup.shadow")
}

description = "Velocity support plugin"

dependencies {
	compileOnly(paper.velocity.api)
	annotationProcessor(paper.velocity.api)

	implementation(project(":commandapi-velocity-core"))
	implementation(project(":commandapi-plugin"))
}

val renameForPublishing = tasks.register<Copy>("renameForPublishing") {
	group = "publishing"
	description = "Copies the shadowJar output and renames the result for publishing to Modrinth"

	// Local, so the rename action doesn't reference the build script (not supported by the configuration cache)
	val jarName = "CommandAPI-$version-Velocity.jar"
	from(tasks.shadowJar)
	into(layout.buildDirectory.dir("publishing"))
	rename { jarName }
}

tasks.named("modrinth") {
	dependsOn(renameForPublishing)
}

modrinth {
	token = providers.environmentVariable("MODRINTH_TOKEN")
	projectId = "commandapi"
	versionNumber = project.version.toString()
	versionType = if (project.version.toString().endsWith("-SNAPSHOT")) "beta" else "release" // adding this so we can potentially publish snapshots for easier access to Modrinth as well
	uploadFile.set(layout.buildDirectory.file("publishing/CommandAPI-${project.version}-Velocity.jar"))
	gameVersions.addAll("1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3")
	loaders.addAll("velocity")

	changelog = rootProject.file("changelog.md").readLines().joinToString("\n")

	debugMode = !providers.gradleProperty("publish-modrinth").getOrElse("false").toBoolean()
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