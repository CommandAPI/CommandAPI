plugins {
	id("buildlogic.java-conventions")
	id("buildlogic.spigot-remap")
}

description = "Bukkit support for 1.21.9"

dependencies {
	compileOnly(spigot.version.v1219) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(spigot.version.v1219)

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
	add("spigot", layout.buildDirectory.file("libs/${project.name}-${project.version}-spigot.jar")) {
		builtBy(tasks.remapObfToSpigot)
	}
	add("mojang", tasks.jar)
}

spigotRemap {
	spigotVersion = "1.21.9"
	sourceJarTask = tasks.shadowJar
}

tasks.withType<Test> {
	failOnNoDiscoveredTests = false
}