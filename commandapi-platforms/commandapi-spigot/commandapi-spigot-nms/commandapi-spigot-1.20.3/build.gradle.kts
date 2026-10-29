plugins {
	id("buildlogic.java-conventions")
	id("buildlogic.spigot-remap")
}

description = "Spigot support for 1.20.3"

dependencies {
	compileOnly(spigot.version.v1204) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(spigot.version.v1204)

	compileOnly(project(":commandapi-bukkit-1.20.3"))
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
	spigotVersion = "1.20.4"
	sourceJarTask = tasks.jar
}