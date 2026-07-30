package publishgithub

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class GitHubPublishPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val extension = project.extensions.create(
			GitHubPublishConfiguration::class.java,
			"github",
			GitHubPublishConfiguration::class.java
		)

		project.tasks.register<GitHubPublishTask>("publishToGitHub") {
			group = "publishing"
			description = "Publishes the CommandAPI plugin artifacts to GitHub"

		}
	}

}