import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
	id("com.modrinth.minotaur")
	id("com.gradleup.shadow")
}

description = "Spigot support Spigot-mapped plugin"

dependencies {
	compileOnly(spigot.version.api)

	implementation(project(":commandapi-spigot-shade"))
	implementation(project(":commandapi-bukkit-plugin-common"))
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
	from("LICENSE")
}

val renameForPublishing = tasks.register("renameForPublishing") {
	group = "publishing"
	description = "Copies the shadowJar output and renames the result for publishing to Modrinth"
	dependsOn(tasks.shadowJar)

	val outputFile = layout.buildDirectory.file("libs/CommandAPI-$version-Spigot.jar")
	inputs.file(tasks.shadowJar.get().archiveFile)
	outputs.file(outputFile)

	doLast {
		copy {
			from(tasks.shadowJar.get().archiveFile)
			into(layout.buildDirectory.dir("libs"))
			rename { "CommandAPI-$version-Spigot.jar" }
		}
	}
}

tasks.named("modrinth") {
	dependsOn(renameForPublishing)
}

modrinth {
	token = System.getenv("MODRINTH_TOKEN")
	projectId = "commandapi"
	versionNumber = project.version.toString()
	versionType = if (project.version.toString().endsWith("-SNAPSHOT")) "beta" else "release" // adding this so we can potentially publish snapshots for easier access to Modrinth as well
	uploadFile.set(renameForPublishing.flatMap { it.outputs.files.let { f -> layout.buildDirectory.file("libs/CommandAPI-$version-Spigot.jar") } })
	gameVersions.addAll("1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2")
	loaders.addAll("spigot")

	changelog = File("changelog.md").readLines().joinToString("\n")

	debugMode = !providers.gradleProperty("publish-modrinth").getOrElse("false").toBoolean()
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
}

afterEvaluate {
	configurations["shadowRuntimeElements"].isCanBeConsumed = false;
	configurations["shadow"].isCanBeConsumed = false
}