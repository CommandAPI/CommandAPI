import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support plugin"

dependencies {
	compileOnly(paper.version.common)

	implementation(project(":commandapi-paper-shade"))
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
	manifest {
		attributes["paperweight-mappings-namespace"] = "mojang"
	}
	from("LICENSE")
}

