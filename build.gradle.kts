import io.papermc.hangarpublishplugin.model.Platforms

plugins {
	id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
	id("com.gradleup.shadow") version "9.6.1" apply false
	id("buildlogic.spigot-remap") apply false
	id("com.modrinth.minotaur") version "2.+" apply false
	id("io.papermc.hangar-publish-plugin") version "0.1.5-SNAPSHOT"
	id("buildlogic.java-conventions")
}

tasks.register("collectPlugins") {
	description = "Collect all plugin jars"
	dependsOn(
		project(":commandapi-paper-plugin").tasks.named("renameForPublishing"),
		project(":commandapi-spigot-plugin").tasks.named("renameForPublishing"),
		project(":commandapi-velocity-plugin").tasks.named("renameForPublishing"),
		project(":commandapi-bukkit-networking-plugin").tasks.named("renameForPublishing")
	)

	doLast {
		copy {
			from(project(":commandapi-paper-plugin").layout.buildDirectory.file("libs/CommandAPI-${project.version}-Paper.jar"))
			into(layout.buildDirectory.dir("libs"))
		}
		copy {
			from(project(":commandapi-spigot-plugin").layout.buildDirectory.file("libs/CommandAPI-${project.version}-Spigot.jar"))
			into(layout.buildDirectory.dir("libs"))
		}
		copy {
			from(project(":commandapi-velocity-plugin").layout.buildDirectory.file("libs/CommandAPI-${project.version}-Velocity.jar"))
			into(layout.buildDirectory.dir("libs"))
		}
		copy {
			from(project(":commandapi-bukkit-networking-plugin").layout.buildDirectory.file("libs/CommandAPI-${project.version}-Networking-Plugin.jar"))
			into(layout.buildDirectory.dir("libs"))
		}
	}
}

afterEvaluate {
	tasks.named("publishPluginPublicationToHangar") {
		dependsOn(tasks.named("collectPlugins"))
	}
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
				jar.set(layout.buildDirectory.file("libs/CommandAPI-${project.version}-Paper.jar"))
				platformVersions = listOf("1.20.6", "1.21.x", "26.1.x", "26.2")
			}
			register(Platforms.VELOCITY) {
				jar.set(layout.buildDirectory.file("libs/CommandAPI-${project.version}-Velocity.jar"))
				platformVersions = listOf("3.3-3.5")
			}
		}
	}
}