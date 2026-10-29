import io.papermc.hangarpublishplugin.model.Platforms

plugins {
	base
	id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
	id("com.gradleup.shadow") version "9.6.1" apply false
	id("buildlogic.spigot-remap") apply false
	id("com.modrinth.minotaur") version "2.10.0" apply false
	id("io.papermc.hangar-publish-plugin") version "0.1.5-SNAPSHOT"
	id("buildlogic.github-publish")
	id("org.sonarqube") version "7.5.0.8588"
}

val changelogText = file("changelog.md").readLines().joinToString("\n")

val collectPlugins = tasks.register<Sync>("collectPlugins") {
	description = "Collect all plugin jars"
	from(
		project(":commandapi-paper-plugin").tasks.named("renameForPublishing"),
		project(":commandapi-spigot-plugin").tasks.named("renameForPublishing"),
		project(":commandapi-velocity-plugin").tasks.named("renameForPublishing"),
		project(":commandapi-bukkit-networking-plugin").tasks.named("renameForPublishing")
	)
	into(layout.buildDirectory.dir("libs"))
}

afterEvaluate {
	tasks.named("publishPluginPublicationToHangar") {
		dependsOn(collectPlugins)
	}
}

hangarPublish {
	publications.register("plugin") {
		version = project.version.toString()
		channel = if (project.version.toString().endsWith("-SNAPSHOT")) "Snapshot" else "Release" // adding this so we can potentially publish snapshots for easier access to Hangar as well
		id = "CommandAPI"
		changelog = changelogText
		apiKey = providers.environmentVariable("HANGAR_API_KEY")
		platforms {
			register(Platforms.PAPER) {
				jar.set(layout.buildDirectory.file("libs/CommandAPI-${project.version}-Paper.jar"))
				platformVersions = listOf("1.20.6", "1.21.x", "26.1.x", "26.2", "26.3")
			}
			register(Platforms.VELOCITY) {
				jar.set(layout.buildDirectory.file("libs/CommandAPI-${project.version}-Velocity.jar"))
				platformVersions = listOf("3.3-3.5")
			}
		}
	}
}

github {
	apiKey = providers.environmentVariable("GITHUB_TOKEN")
	version = project.version.toString()
	changelog = changelogText
	preRelease = project.version.toString().endsWith("-SNAPSHOT")
	files.from(collectPlugins)
}

sonar {
	properties {
		property("sonar.projectKey", "JorelAli_CommandAPI")
		property("sonar.organization", "jorel-ali")
		property("sonar.host.url", "https://sonarcloud.io")
	}
}
