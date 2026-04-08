plugins {
	id("buildlogic.java-conventions")
	id("io.typst.gradlesource.spigot")
}

description = "Bukkit support for 1.20.2"

dependencies {
	compileOnly(spigot.version.v1202) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(spigot.version.v1202)

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
	add("spigot", layout.buildDirectory.file("libs/${project.name}-${project.version}-spigot-jar.jar")) {
		builtBy(tasks.remapObfToSpigot)
	}
	add("mojang", tasks.jar)
}

spigotRemap {
	spigotVersion = "1.20.2"
	sourceJarTask = tasks.jar
}

tasks.withType<Test> {
	failOnNoDiscoveredTests = false
}