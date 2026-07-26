import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
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
			rename { "CommandAPI-$version-Networking-Plugin.jar" }
		}
	}
}