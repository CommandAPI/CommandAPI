plugins {
	`java-library`
}

repositories {
	mavenCentral()
	maven {
		url = uri("https://libraries.minecraft.net")
		mavenContent {
			includeGroup("com.mojang")
		}
	}
	maven {
		url = uri("https://repo.codemc.io/repository/maven-public/")
	}
	maven {
		url = uri("https://repo.codemc.io/repository/nms/")
	}
	maven {
		url = uri("https://repo.papermc.io/repository/maven-public/")
	}
	maven {
		url = uri("https://central.sonatype.com/repository/maven-snapshots/")
		mavenContent {
			snapshotsOnly()
		}
	}
	mavenLocal {
		mavenContent {
			includeModule("org.spigotmc", "spigot")
		}
	}
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	// Unlike sourceCompatibility/targetCompatibility, this also checks that only Java 17 APIs are used
	options.release = 17
	options.compilerArgs.add("-Xpkginfo:always")
}

tasks.withType<Javadoc>().configureEach {
	options.encoding = "UTF-8"
	options {
		this as StandardJavadocDocletOptions
		tags(
			"apiNote:a:API Note:",
		)
	}
}

tasks.named("test") {
	dependsOn(tasks.jar)
}

configurations.configureEach {
	if (isCanBeResolved) {
		attributes {
			attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
		}
	}
	if (name in listOf("apiElements", "runtimeElements")) {
		outgoing.artifacts.clear()
		outgoing.artifact(tasks.jar)

		pluginManager.withPlugin("com.gradleup.shadow") {
			outgoing.artifacts.clear()
			outgoing.artifact(tasks.named("shadowJar"))
		}
	}
}
