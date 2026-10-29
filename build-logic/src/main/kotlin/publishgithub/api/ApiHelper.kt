package publishgithub.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.gradle.api.logging.Logger
import java.io.File
import java.net.HttpURLConnection
import java.net.URI

class ApiHelper {

	companion object {

		@JvmStatic
		fun get(url: String): String {
			val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
			connection.requestMethod = "GET"

			connection.setRequestProperty("Accept", "application/vnd.github+json")

			return connection.inputStream.bufferedReader().use { it.readText() }
		}

		@JvmStatic
		fun delete(url: String, apiKey: String): Int {
			val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
			connection.requestMethod = "DELETE"

			connection.setRequestProperty("Accept", "application/vnd.github+json")
			connection.setRequestProperty("Authorization", "Bearer $apiKey")

			return connection.responseCode
		}

		@JvmStatic
		fun create(url: String, apiKey: String, version: String, prerelease: Boolean, changelog: String, logger: Logger): String {
			val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
			connection.requestMethod = "POST"

			connection.setRequestProperty("Accept", "application/vnd.github+json")
			connection.setRequestProperty("Authorization", "Bearer $apiKey")

			connection.doOutput = true

			val releaseObject = JsonObject()
			releaseObject.addProperty("tag_name", version)
			releaseObject.addProperty("name", "CommandAPI Version $version")
			releaseObject.addProperty("target_commitish", if (prerelease) "dev/dev" else "release/$version")
			releaseObject.addProperty("body", changelog)
			releaseObject.addProperty("prerelease", prerelease)

			connection.outputStream.use { it.write(Gson().toJson(releaseObject).toByteArray()) }

			val responseCode = connection.responseCode
			if (responseCode == 201) {
				return connection.inputStream.bufferedReader().use { it.readText() }
			}
			val responseBody = connection.errorStream.bufferedReader().use { it.readText() }

			logger.lifecycle("Response code: $responseCode")
			logger.lifecycle("Response body: $responseBody")

			throw IllegalStateException("Something went wrong while trying to create release $version")
		}

		@JvmStatic
		fun uploadFile(url: String, apiKey: String, file: File, logger: Logger) {
			val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
			connection.requestMethod = "POST"

			connection.setRequestProperty("Accept", "application/vnd.github+json")
			connection.setRequestProperty("Authorization", "Bearer $apiKey")
			connection.setRequestProperty("Content-Type", "application/java-archive")

			connection.doOutput = true

			connection.outputStream.use { it.write(file.readBytes()) }

			val responseCode = connection.responseCode
			if (responseCode == 201) {
				return
			}
			val responseBody = connection.errorStream.bufferedReader().use { it.readText() }

			logger.lifecycle("Response code: $responseCode")
			logger.lifecycle("Response body: $responseBody")
		}

	}

}
