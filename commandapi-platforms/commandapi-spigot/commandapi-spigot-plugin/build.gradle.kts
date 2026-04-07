import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support Spigot-mapped plugin"

dependencies {
	compileOnly(spigot.version.api)

	implementation(project(":commandapi-spigot-shade"))
	implementation(project(":commandapi-bukkit-plugin-common"))
}

tasks.withType<ProcessResources> {
	val properties = mapOf(
		"version" to version,
	)
	inputs.properties(properties)
	filesMatching("plugin.yml") {
		expand(properties)
	}
}

tasks.withType<ShadowJar> {
	from("LICENSE")
}