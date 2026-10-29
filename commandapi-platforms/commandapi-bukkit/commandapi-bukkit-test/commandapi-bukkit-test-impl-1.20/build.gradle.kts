plugins {
	id("buildlogic.java-conventions")
	id("buildlogic.spigot-remap")
}

description = "CommandAPI - Bukkit support test library implementation for 1.20"

dependencies {
	compileOnly(spigot.version.v1201) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.de.tr7zw.item.nbt.api)

	implementation(libs.org.mockito.mockito.core)
	implementation("com.github.seeseemelk:MockBukkit-v1.20:3.9.0")
	api(project(":commandapi-bukkit-test-impl"))

	// Previously commandapi-bukkit-shade. The NMS classes are compileOnly here, so consumers
	// can pick either the Spigot or Mojang mapped variant to match this module's variant
	compileOnly(project(":commandapi-bukkit-nms-common", "mojang"))
	compileOnly(project(":commandapi-bukkit-1.20", "mojang"))

	testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
}

val mappingAttribute = Attribute.of("mappingType", String::class.java)

configurations.create("spigot") {
	isCanBeConsumed = true
	isCanBeResolved = false
	extendsFrom(configurations.implementation.get(), configurations.runtimeOnly.get())
	attributes {
		attribute(mappingAttribute, "spigot")
	}
}

configurations.create("mojang") {
	isCanBeConsumed = true
	isCanBeResolved = false
	extendsFrom(configurations.implementation.get(), configurations.runtimeOnly.get())
	attributes {
		attribute(mappingAttribute, "mojang")
	}
}

artifacts {
	add("spigot", tasks.remapObfToSpigot.flatMap { it.outputJarFile })
	add("mojang", tasks.jar)
}

spigotRemap {
	spigotVersion = "1.20.1"
	sourceJarTask = tasks.jar
}

tasks.withType<Test> {
	failOnNoDiscoveredTests = false
}
