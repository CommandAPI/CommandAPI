package publishgithub

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property

abstract class GitHubPublishConfiguration {

	abstract val apiKey: Property<String>

	abstract val version: Property<String>

	abstract val preRelease: Property<Boolean>

	abstract val changelog: Property<String>

	abstract val files: ConfigurableFileCollection

}
