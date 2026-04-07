plugins {
	id("buildlogic.java-conventions")
	id("io.typst.gradlesource.spigot")
}

description = "Spigot support for 1.21.5"

dependencies {
	compileOnly(spigot.version.v1215) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(spigot.version.v1215)

	compileOnly(project(":commandapi-bukkit-1.21.5"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	api(project(":commandapi-spigot-core"))
}

configurations.create("remapped") {
	isCanBeConsumed = true
	isCanBeResolved = false
}

artifacts {
	add("remapped", layout.buildDirectory.file("libs/${project.name}-${project.version}-spigot-jar.jar")) {
		builtBy(tasks.remapObfToSpigot)
	}
}

spigotRemap {
	spigotVersion = "1.21.5"
	sourceJarTask = tasks.jar
}