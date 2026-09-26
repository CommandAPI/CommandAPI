package publishgithub.api

import org.gradle.internal.impldep.kotlinx.serialization.json.Json
import org.jetbrains.kotlin.com.google.gson.JsonParser

class ReleaseInfo {

	private val URL = "https://api.github.com/repos/CommandAPI/CommandAPI/releases"

	fun getReleaseTags(): List<Triple<String, Boolean, Long>> {
		val jsonString = ApiHelper.get(URL)
		val json = JsonParser.parseString(jsonString).asJsonArray
		val tags = mutableListOf<Triple<String, Boolean, Long>>()
		for (element in json) {
			tags.add(
				Triple(element.asJsonObject.get("tag_name").asString,
					element.asJsonObject.get("prerelease").asBoolean,
					element.asJsonObject.get("id").asLong)
			)
		}
		return tags
	}

}