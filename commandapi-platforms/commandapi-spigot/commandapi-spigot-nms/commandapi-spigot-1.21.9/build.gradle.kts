import io.github.patrick.gradle.remapper.RemapTask

plugins {
	id("buildlogic.java-conventions")
	id("io.github.patrick.remapper")
}

description = "Spigot support for 1.21.9"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1219) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(libs.org.spigotmc.spigot.v1219)

	compileOnly(project(":commandapi-bukkit-1.21.9"))
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
	version = "1.21.9"
	archiveClassifier = "remapped"
}