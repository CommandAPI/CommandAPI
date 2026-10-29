plugins {
	id("buildlogic.java-conventions")
	id("buildlogic.spigot-remap")
}

description = "Spigot support for 1.21.11"

dependencies {
	compileOnly(spigot.version.v12111) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(spigot.version.v12111)

	compileOnly(project(":commandapi-bukkit-1.21.11"))
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
	spigotVersion = "1.21.11"
	sourceJarTask = tasks.jar
}