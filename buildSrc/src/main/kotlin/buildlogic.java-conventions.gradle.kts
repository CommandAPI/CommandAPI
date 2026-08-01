plugins {
    `java-library`
}

repositories {
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    maven {
        url = uri("https://libraries.minecraft.net")
    }
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/nms/")
    }
	maven {
		url = uri("https://repo.papermc.io/repository/maven-public/")
	}
	maven {
		url = uri("https://central.sonatype.com/repository/maven-snapshots/")
	}
	mavenCentral()
}

group = "dev.jorel"
version = "12.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
	sourceCompatibility = "17"
	targetCompatibility = "17"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
	options {
		this as StandardJavadocDocletOptions
		tags(
			"apiNote:a:API Note:",
		)
	}
}

tasks.named("build") {
	dependsOn(tasks.jar)
}

tasks.named("test") {
	dependsOn(tasks.jar)
}

configurations.all {
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
