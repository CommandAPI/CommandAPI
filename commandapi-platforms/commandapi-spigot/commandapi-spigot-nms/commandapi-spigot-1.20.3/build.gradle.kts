import io.github.patrick.gradle.remapper.RemapTask

plugins {
	id("buildlogic.java-conventions")
	id("io.github.patrick.remapper")
}

description = "Spigot support for 1.20.3"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1204) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(libs.org.spigotmc.spigot.v1204)

	compileOnly(project(":commandapi-bukkit-1.20.3"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	api(project(":commandapi-spigot-core"))
}

configurations.create("remapped") {
	isCanBeConsumed = true
	isCanBeResolved = false
}

artifacts {
	add("remapped", layout.buildDirectory.file("libs/${project.name}-${project.version}-remapped.jar")) {
		builtBy(tasks.remap)
	}
}

tasks.withType<RemapTask> {
	version = "1.20.4"
	archiveClassifier = "remapped"
}