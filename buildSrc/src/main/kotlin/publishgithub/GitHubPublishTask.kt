package publishgithub

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.TaskAction
import publishgithub.api.Release
import publishgithub.api.ReleaseInfo
import java.io.File

abstract class GitHubPublishTask : DefaultTask() {

	@TaskAction
	fun run() {
		val configuration = project.extensions.getByType(GitHubPublishConfiguration::class.java)
		val apiKey: String = configuration.apiKey.get()
		val version: String = configuration.version.get()
		val changelog: String = configuration.changelog.get()
		val preRelease: Boolean = configuration.preRelease.get()
		val files: FileCollection = configuration.files.get()

		/**
		 * Step 0: Check if current version is a prerelease. If not, do nothing
		 * Step 1: Get all releases and check if the current version is already present
		 * Step 1.5: Verify there's at most one prerelease
		 * Step 2: If there is currently a prerelease version that is not the current version, delete that
		 * Step 3: If the current version is already published and it is a snapshot version, update the jar files
		 * Step 4: If the current version isn't published, create a new release
		 * Step 5: Profit!
		 */
		val releases = ReleaseInfo().getReleaseTags()

		var info: Triple<String, Boolean, Long>? = null
		var prereleases = 0
		for (release in releases) {
			if (release.second && prereleases == 0) {
				prereleases++
				info = release
				continue
			}
			if (release.second && prereleases > 0) {
				throw IllegalStateException("There is more than one prerelease! Please delete them manually!")
			}
		}

		val release = Release(apiKey, version, preRelease, changelog, project.logger, info)

		if (info != null && info.first != version) {
			release.deleteVersion()
		}

		if (info == null || info.first == version) {
			release.createOrUpdate(files.files)
		}

		project.logger.lifecycle("Released version $version to GitHub!")
	}

}