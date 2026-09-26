package publishgithub

import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

abstract class GitHubPublishConfiguration {

	@get:Input
	abstract val apiKey: Property<String>

	@get:Input
	abstract val version: Property<String>

	@get:Input
	abstract val preRelease: Property<Boolean>

	@get:Input
	abstract val changelog: Property<String>

	@get:Input
	abstract val files: Property<FileCollection>

}