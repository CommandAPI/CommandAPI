pluginManagement {
	includeBuild("build-logic")
	repositories {
		gradlePluginPortal()
		maven("https://repo.papermc.io/repository/maven-public/")
	}
}

plugins {
	// Downloads the Java 17/21 toolchains used by the Kotlin modules and the Bukkit tests if they aren't installed
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
	versionCatalogs {
		create("paper") {
			from(files("gradle/paper.versions.toml"))
		}
		create("spigot") {
			from(files("gradle/spigot.versions.toml"))
		}
	}
}

rootProject.name = "commandapi"

val profile = providers.gradleProperty("profile").getOrElse("default")

val permanentlyExcluded = listOf("sponge")
val excludedModules = mapOf(
	"default" to setOf(),
	"bukkit" to setOf("velocity"),
	"paper" to setOf("spigot", "velocity"),
	"spigot" to setOf("paper", "velocity"),
	"velocity" to setOf("bukkit", "paper", "spigot", "commandapi-annotations")
)
val excluded = (excludedModules[profile] ?: error("Unknown profile: $profile! Please choose one of ${excludedModules.keys}")) + permanentlyExcluded

// Directories that never contain modules, so we don't have to walk through them
val ignoredDirectories = setOf("src", "build", "out", ".gradle")

fun canBuildModule(name: String): Boolean = excluded.none { name.contains(it) }

fun includeModules(dir: File, path: String) {
	if (canBuildModule(dir.name) && dir.resolve("build.gradle.kts").exists()) {
		include(":${dir.name}")
		project(":${dir.name}").projectDir = dir
	}
	dir.listFiles()?.forEach { entry ->
		if (entry.isDirectory && entry.name !in ignoredDirectories && canBuildModule("$path:${entry.name}")) {
			includeModules(entry, "$path:${entry.name}")
		}
	}
}

includeModules(file("commandapi-annotations"), ":commandapi-annotations")
includeModules(file("commandapi-codecov"), ":commandapi-codecov")
includeModules(file("commandapi-core"), ":commandapi-core")
includeModules(file("commandapi-kotlin"), ":commandapi-kotlin")
includeModules(file("commandapi-platforms"), ":commandapi-platforms")
includeModules(file("commandapi-plugin"), ":commandapi-plugin")
includeModules(file("commandapi-preprocessor"), ":commandapi-preprocessor")
includeModules(file("commandapi-testing"), ":commandapi-testing")
