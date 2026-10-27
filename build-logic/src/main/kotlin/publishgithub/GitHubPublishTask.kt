package publishgithub

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import publishgithub.api.Release
import publishgithub.api.ReleaseInfo

abstract class GitHubPublishTask : DefaultTask() {

	@get:Internal
	abstract val apiKey: Property<String>

	@get:Input
	abstract val version: Property<String>

	@get:Input
	abstract val preRelease: Property<Boolean>

	@get:Input
	abstract val changelog: Property<String>

	@get:InputFiles
	abstract val files: ConfigurableFileCollection

	init {
		// Publishing has side effects outside the build, so this task should never be up-to-date
		outputs.upToDateWhen { false }
	}

	@TaskAction
	fun run() {
		val apiKey: String = apiKey.get()
		val version: String = version.get()
		val changelog: String = changelog.get()
		val preRelease: Boolean = preRelease.get()

		/**
		 * Step 1: Get all releases and find the current prerelease
		 * Step 1.5: Verify there's at most one prerelease
		 * Step 2: If there is currently a prerelease version that is not the current version, delete that
		 * Step 3: If the current version is already published as a prerelease, update the jar files
		 * Step 4: Otherwise, create a new release
		 * Step 5: Profit!
		 */
		val prereleases = ReleaseInfo().getReleaseTags().filter { it.second }
		check(prereleases.size <= 1) { "There is more than one prerelease! Please delete them manually!" }
		var info = prereleases.firstOrNull()

		if (info != null && info.first != version) {
			Release(apiKey, version, preRelease, changelog, logger, info).deleteVersion()
			info = null
		}

		Release(apiKey, version, preRelease, changelog, logger, info).createOrUpdate(files.asFileTree.files)

		logger.lifecycle("Released version $version to GitHub!")
	}

}
