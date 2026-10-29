import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
	id("com.modrinth.minotaur")
	id("com.gradleup.shadow")
}

description = "Paper support plugin"

val commandApiShade = project(":commandapi-paper-shade")

evaluationDependsOn(":commandapi-paper-shade")

dependencies {
	compileOnly(paper.version.common)

	implementation(files(commandApiShade.tasks.named("shadowJar")))
	implementation(project(":commandapi-bukkit-plugin-common"))
}

tasks.withType<ProcessResources> {
	val properties = mapOf(
		"version" to version,
	)
	inputs.properties(properties)
	filesMatching("paper-plugin.yml") {
		expand(properties)
	}
}

tasks.withType<ShadowJar> {
	manifest {
		attributes["paperweight-mappings-namespace"] = "mojang"
	}
	from("LICENSE")
}

val renameForPublishing = tasks.register<Copy>("renameForPublishing") {
	group = "publishing"
	description = "Copies the shadowJar output and renames the result for publishing to Modrinth"

	// Local, so the rename action doesn't reference the build script (not supported by the configuration cache)
	val jarName = "CommandAPI-$version-Paper.jar"
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
	uploadFile.set(layout.buildDirectory.file("publishing/CommandAPI-${project.version}-Paper.jar"))
	gameVersions.addAll("1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3")
	loaders.addAll("paper", "folia")

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
