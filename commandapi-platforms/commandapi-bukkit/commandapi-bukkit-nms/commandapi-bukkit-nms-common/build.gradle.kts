plugins {
	id("buildlogic.java-conventions")
	id("io.typst.gradlesource.spigot")
}

description = "Bukkit support NMS common library"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1201) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	compileOnly(libs.paper.version.bukkit.common)

	compileOnly(project(":commandapi-bukkit-core"))
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
	spigotVersion = "1.20.1"
	sourceJarTask = tasks.jar
}