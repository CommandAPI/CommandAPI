plugins {
	id("buildlogic.java-conventions")
	id("buildlogic.spigot-remap")
}

description = "CommandAPI - Spigot support test library implementation for 1.20.3"

dependencies {
	compileOnly(spigot.version.v1204) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.org.mockito.mockito.core)

	compileOnly(project(":commandapi-spigot-core"))
	compileOnly(project(":commandapi-spigot-1.20.3"))
	compileOnly(project(":commandapi-bukkit-nms-common", "mojang"))
	compileOnly(project(":commandapi-bukkit-1.20.3", "mojang"))

	// The Bukkit test implementation is compileOnly here, so consumers can
	// pick either the Spigot or Mojang mapped variant to match this module's variant
	compileOnly(project(":commandapi-bukkit-test-impl-1.20.3", "mojang"))
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
	spigotVersion = "1.20.4"
	sourceJarTask = tasks.jar
}

tasks.withType<Test> {
	failOnNoDiscoveredTests = false
}
