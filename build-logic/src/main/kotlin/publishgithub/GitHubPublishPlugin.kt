package publishgithub

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class GitHubPublishPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val extension = project.extensions.create<GitHubPublishConfiguration>("github")

		project.tasks.register<GitHubPublishTask>("publishToGitHub") {
			group = "publishing"
			description = "Publishes the CommandAPI plugin artifacts to GitHub"

			apiKey.set(extension.apiKey)
			version.set(extension.version)
			preRelease.set(extension.preRelease)
			changelog.set(extension.changelog)
			files.from(extension.files)
		}
	}

}
