import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
	id("com.gradleup.shadow")
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
version = "11.2.1-SNAPSHOT"

java {
    withSourcesJar()
	withJavadocJar()
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifact(tasks["shadowJar"]) {
			classifier = ""
        }
	    artifact(tasks["sourcesJar"]) {
			classifier = "sources"
	    }
	    artifact(tasks["javadocJar"]) {
			classifier = "javadoc"
	    }
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
	dependsOn("shadowJar")
}

tasks.named("test") {
	dependsOn("shadowJar")
}

configurations.all {
	if (isCanBeResolved) {
		attributes {
			attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
		}
	}
	if (name in listOf("apiElements", "runtimeElements")) {
		outgoing.artifacts.clear()
		outgoing.artifact(tasks.shadowJar)
	}
}

afterEvaluate {
	configurations["shadowRuntimeElements"].isCanBeConsumed = false;
	configurations["shadow"].isCanBeConsumed = false
}
