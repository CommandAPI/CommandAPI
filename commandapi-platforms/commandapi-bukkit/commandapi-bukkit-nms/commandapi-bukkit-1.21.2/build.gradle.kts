import io.github.patrick.gradle.remapper.RemapTask

plugins {
	id("buildlogic.java-conventions")
	id("io.github.patrick.remapper")
}

description = "Bukkit support for 1.21.2"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1213) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(libs.org.spigotmc.spigot.v1213)

	compileOnly(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
	testCompileOnly(project(":commandapi-preprocessor"))
	testAnnotationProcessor(project(":commandapi-preprocessor"))
}

val mappingAttribute = Attribute.of("mappingType", String::class.java)

configurations.create("spigot") {
	isCanBeConsumed = true
	isCanBeResolved = false
	attributes {
		attribute(mappingAttribute, "spigot")
	}
}
configurations.create("mojang") {
	isCanBeConsumed = true
	isCanBeResolved = false
	attributes {
		attribute(mappingAttribute, "mojang")
	}
}

artifacts {
	add("spigot", layout.buildDirectory.file("libs/${project.name}-${project.version}-remapped.jar")) {
		builtBy(tasks.remap)
	}
	add("mojang", tasks.jar)
}

tasks.withType<RemapTask> {
	version = "1.21.3"
	archiveClassifier = "remapped"
}

tasks.withType<Test> {
	failOnNoDiscoveredTests = false
}