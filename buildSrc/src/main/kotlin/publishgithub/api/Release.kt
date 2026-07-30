package publishgithub.api

import org.gradle.api.logging.Logger
import org.jetbrains.kotlin.com.google.gson.JsonParser
import java.io.File

class Release constructor(
	val apiKey: String,
	val commandAPIVersion: String,
	val prerelease: Boolean,
	val changelog: String,
	val logger: Logger,
	val version: Triple<String, Boolean, Long>? = null) {

	fun createOrUpdate(files: MutableSet<File>) {
		if (version == null) {
			create(files)
		} else {
			update(files)
		}
	}

	fun deleteVersion() {
		val toDelete = version!!
		val releaseUrl = "https://api.github.com/repos/CommandAPI/CommandAPI/releases/${toDelete.third}"
		var code = ApiHelper.delete(releaseUrl, apiKey)
		if (code != 204) {
			throw IllegalStateException("Something went wrong while trying to delete release ${toDelete.first}")
		}
		val tagUrl = "https://api.github.com/repos/CommandAPI/CommandAPI/git/refs/tags/$commandAPIVersion"
	}

	private fun create(files: MutableSet<File>) {
		val url = "https://api.github.com/repos/CommandAPI/CommandAPI/releases"
		val json = ApiHelper.create(url, apiKey, commandAPIVersion, prerelease, changelog, logger)

		val response = JsonParser.parseString(json).asJsonObject

		for (file in files) {
			val fileUrl = "https://uploads.github.com/repos/CommandAPI/CommandAPI/releases/${response.get("id").asLong}/assets?name=${file.name}"
			ApiHelper.uploadFile(fileUrl, apiKey, file, logger)
		}
	}

	private fun update(files: MutableSet<File>) {
		val uploadedRelease = ReleaseInfo().getReleaseTags().find { it.second }!!

		val url = "https://api.github.com/repos/CommandAPI/CommandAPI/releases/${uploadedRelease.third}/assets"
		val json = ApiHelper.get(url)

		val assetIds = mutableListOf<Long>()
		val elements = JsonParser.parseString(json).asJsonArray
		for (element in elements) {
			assetIds.add(element.asJsonObject.get("id").asLong)
		}

		for (assetId in assetIds) {
			val assetUrl = "https://api.github.com/repos/CommandAPI/CommandAPI/releases/assets/$assetId"
			ApiHelper.delete(assetUrl, apiKey)
		}

		for (file in files) {
			val fileUrl = "https://uploads.github.com/repos/CommandAPI/CommandAPI/releases/${uploadedRelease.third}/assets?name=${file.name}"
			ApiHelper.uploadFile(fileUrl, apiKey, file, logger)
		}
	}

}