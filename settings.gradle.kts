pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://repo.papermc.io/repository/maven-public/")
	}
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

val permanentlyExcluded = listOf("sponge", "bukkit-test:")
val excludedModules = mapOf(
	"default" to setOf(),
	"bukkit" to setOf("velocity"),
	"paper" to setOf("spigot", "velocity"),
	"spigot" to setOf("paper", "velocity"),
	"velocity" to setOf("bukkit", "paper", "spigot", "commandapi-annotations")
)

fun canBuildModule(name: String): Boolean {
	val excluded = excludedModules[profile] ?: error("Unknown profile: $profile! Please choose one of ${excludedModules.keys}")
	for (exclusion in excluded) {
		if (name.contains(exclusion)) {
			return false;
		}
	}
	for (exclusion in permanentlyExcluded) {
		if (name.contains(exclusion)) {
			return false;
		}
	}
	return true;
}

fun includeModules(dir: File, path: String) {
	if (canBuildModule(dir.name) && dir.resolve("build.gradle.kts").exists()) {
		include(":${dir.name}")
		findProject(":${dir.name}")!!.projectDir = dir
	}
	dir.listFiles()?.forEach { entry ->
		if (entry.isDirectory && canBuildModule("$path:${entry.name}")) {
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
