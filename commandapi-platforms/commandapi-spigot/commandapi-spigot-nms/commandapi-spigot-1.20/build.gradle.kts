plugins {
	id("buildlogic.java-conventions")
	id("buildlogic.spigot-remap")
}

description = "Spigot support for 1.20"

dependencies {
	compileOnly(spigot.version.v1201) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(spigot.version.v1201)

	compileOnly(project(":commandapi-bukkit-1.20"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	api(project(":commandapi-spigot-core"))
}

configurations.create("remapped") {
	isCanBeConsumed = true
	isCanBeResolved = false
}

artifacts {
	add("remapped", tasks.remapObfToSpigot.flatMap { it.outputJarFile })
}

spigotRemap {
	spigotVersion = "1.20.1"
	sourceJarTask = tasks.jar
}