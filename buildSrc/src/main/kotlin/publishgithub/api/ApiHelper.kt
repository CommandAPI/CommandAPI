package publishgithub.api

import org.gradle.api.logging.Logger
import org.jetbrains.kotlin.com.google.gson.Gson
import org.jetbrains.kotlin.com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

class ApiHelper {

	companion object {

		@JvmStatic
		fun get(url: String): String {
			val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
			connection.requestMethod = "GET"

			connection.setRequestProperty("Accept", "application/vnd.github+json")

			val inputStream = connection.inputStream
			val reader = BufferedReader(InputStreamReader(inputStream))
			var line = reader.readLine()
			val builder = StringBuilder()
			while (line != null) {
				builder.append(line)
				line = reader.readLine()
			}
			reader.close()
			return builder.toString()
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
			releaseObject.addProperty("target_commitish", "dev/dev")
			releaseObject.addProperty("body", changelog)
			releaseObject.addProperty("prerelease", prerelease)

			connection.outputStream.write(Gson().toJson(releaseObject).toByteArray())
			connection.outputStream.close()

			val responseCode = connection.responseCode
			if (responseCode == 201) {
				return connection.inputStream.bufferedReader().use { it.readText() }
			}
			val responseBody = connection.errorStream.bufferedReader().use { it.readText() }

			logger.lifecycle("Response code: $responseCode")
			logger.lifecycle("Response body: $responseBody")

			return "";
		}

		@JvmStatic
		fun uploadFile(url: String, apiKey: String, file: File, logger: Logger) {
			val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
			connection.requestMethod = "POST"

			connection.setRequestProperty("Accept", "application/vnd.github+json")
			connection.setRequestProperty("Authorization", "Bearer $apiKey")
			connection.setRequestProperty("Content-Type", "application/jar")

			connection.doOutput = true

			connection.outputStream.write(file.readBytes())

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