import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.hangarpublishplugin.model.Platforms

plugins {
	id("buildlogic.java-conventions")
	id("com.modrinth.minotaur")
	id("io.papermc.hangar-publish-plugin")
}

description = "Paper support plugin"

dependencies {
	compileOnly(paper.version.common)

	implementation(project(":commandapi-paper-shade"))
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
	manifest {
		attributes["paperweight-mappings-namespace"] = "mojang"
	}
	from("LICENSE")
}

val renameForPublishing = tasks.register("renameForPublishing") {
	group = "publishing"
	description = "Copies the shadowJar output and renames the result for publishing to Modrinth"
	dependsOn(tasks.shadowJar)

	val outputFile = layout.buildDirectory.file("libs/CommandAPI-$version-Paper.jar")
	inputs.file(tasks.shadowJar.get().archiveFile)
	outputs.file(outputFile)

	doLast {
		copy {
			from(tasks.shadowJar.get().archiveFile)
			into(layout.buildDirectory.dir("libs"))
			rename { "CommandAPI-$version-Paper.jar" }
		}
	}
}

tasks.named("modrinth") {
	dependsOn(renameForPublishing)
}

afterEvaluate {
	tasks.named("publishPluginPublicationToHangar") {
		dependsOn(renameForPublishing)
	}
}

modrinth {
	token = System.getenv("MODRINTH_TOKEN")
	projectId = "commandapi"
	versionNumber = project.version.toString()
	versionType = if (project.version.toString().endsWith("-SNAPSHOT")) "beta" else "release" // adding this so we can potentially publish snapshots for easier access to Modrinth as well
	uploadFile.set(renameForPublishing.flatMap { it.outputs.files.let { _ -> layout.buildDirectory.file("libs/CommandAPI-${project.version}-Paper.jar") } })
	gameVersions.addAll("1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2")
	loaders.addAll("paper", "folia")

	changelog = File("changelog.md").readLines().joinToString("\n")

	debugMode = !providers.gradleProperty("publish-modrinth").getOrElse("false").toBoolean()
}

hangarPublish {
	publications.register("plugin") {
		version = project.version.toString()
		channel = if (project.version.toString().endsWith("-SNAPSHOT")) "Snapshot" else "Release" // adding this so we can potentially publish snapshots for easier access to Hangar as well
		id = "CommandAPI"
		changelog = File("changelog.md").readLines().joinToString("\n")
		apiKey = System.getenv("HANGAR_API_KEY")
		platforms {
			register(Platforms.PAPER) {
				jar.set(renameForPublishing.flatMap { it.outputs.files.let { _ -> layout.buildDirectory.file("libs/CommandAPI-${project.version}-Paper.jar") } })
				platformVersions = listOf("1.20.6", "1.21.x", "26.1.x", "26.2")
			}
		}
	}
}
