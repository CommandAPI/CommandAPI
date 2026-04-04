rootProject.name = "commandapi"

val profile = settings.extra.properties["profile"] as String? ?: "default"

val permanentlyExcluded = listOf("sponge", "bukkit-test")
val excludedModules = mapOf(
	"default" to setOf(),
	"bukkit" to setOf("velocity"),
	"paper" to setOf("spigot", "velocity"),
	"spigot" to setOf("paper", "velocity"),
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
		if (entry.isDirectory && canBuildModule(entry.name)) {
			includeModules(entry, "$path:${entry.name}")
		}
	}
}

fun include(dir: File, projectPath: String) {
	include(projectPath)
	val project = findProject(projectPath)!!
	project.projectDir = dir
	project.name = dir.name
}

includeModules(file("commandapi-annotations"), ":commandapi-annotations")
includeModules(file("commandapi-codecov"), ":commandapi-codecov")
includeModules(file("commandapi-core"), ":commandapi-core")
includeModules(file("commandapi-kotlin"), ":commandapi-kotlin")
includeModules(file("commandapi-platforms"), ":commandapi-platforms")
includeModules(file("commandapi-plugin"), ":commandapi-plugin")
includeModules(file("commandapi-preprocessor"), ":commandapi-preprocessor")
includeModules(file("commandapi-testing"), ":commandapi-testing")

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://papermc.io/repo/repository/maven-public/")
	}
}
