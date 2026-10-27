package publishgithub.api

import com.google.gson.JsonParser
import org.gradle.api.logging.Logger
import java.io.File

class Release(
	val apiKey: String,
	val commandAPIVersion: String,
	val prerelease: Boolean,
	val changelog: String,
	val logger: Logger,
	val version: Triple<String, Boolean, Long>? = null) {

	fun createOrUpdate(files: Set<File>) {
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
			throw IllegalStateException("Something went wrong while trying to delete release ${toDelete.first} (response code ${code})")
		}
		val tagUrl = "https://api.github.com/repos/CommandAPI/CommandAPI/git/refs/tags/${toDelete.first}"
		code = ApiHelper.delete(tagUrl, apiKey)
		if (code != 204) {
			throw IllegalStateException("Something went wrong while trying to delete tag ${toDelete.first} (response code ${code})")
		}
	}

	private fun create(files: Set<File>) {
		val url = "https://api.github.com/repos/CommandAPI/CommandAPI/releases"
		val json = ApiHelper.create(url, apiKey, commandAPIVersion, prerelease, changelog, logger)

		val response = JsonParser.parseString(json).asJsonObject

		for (file in files) {
			val fileUrl = "https://uploads.github.com/repos/CommandAPI/CommandAPI/releases/${response.get("id").asLong}/assets?name=${file.name}"
			ApiHelper.uploadFile(fileUrl, apiKey, file, logger)
		}
	}

	private fun update(files: Set<File>) {
		val releaseId = version!!.third

		val url = "https://api.github.com/repos/CommandAPI/CommandAPI/releases/$releaseId/assets"
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
			val fileUrl = "https://uploads.github.com/repos/CommandAPI/CommandAPI/releases/$releaseId/assets?name=${file.name}"
			ApiHelper.uploadFile(fileUrl, apiKey, file, logger)
		}
	}

}
