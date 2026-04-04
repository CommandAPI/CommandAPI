rootProject.name = "commandapi"

val profile = settings.extra.properties["profile"] as String? ?: "default"

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
	return true;
}

fun includeModules(project: File, path: String) {
	if (canBuildModule(project.name) && project.resolve("build.gradle.kts").exists()) {
		include(path)
		findProject(path)!!.projectDir = project
	}
	project.listFiles()?.forEach { entry ->
		if (entry.isDirectory && canBuildModule(entry.name)) {
			val projectPath = "$path:${entry.name}"
			if (entry.resolve("build.gradle.kts").exists()) {
				include(projectPath)
				findProject(projectPath)!!.projectDir = entry
			}
			includeModules(entry, projectPath)
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
