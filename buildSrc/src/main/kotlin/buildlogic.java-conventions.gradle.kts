plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.37.0"
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

// https://vanniktech.github.io/gradle-maven-publish-plugin/central/
mavenPublishing {
  publishToMavenCentral()

  if (!version.toString().endsWith("SNAPSHOT")) {
	// Don't sign SNAPSHOT versions, only main releases
  	signAllPublications()
  }

  coordinates(group.toString(), name.toString(), version.toString())

  pom {
    name.set("commandapi")
    description.set("A Bukkit/Spigot API for the command UI introduced in Minecraft 1.13")
    inceptionYear.set("2018")
    url.set("https://docs.commandapi.dev/")
    licenses {
      license {
        name.set("MIT License")
        url.set("http://www.opensource.org/licenses/mit-license.php")
      }
    }
    developers {
      developer {
        id.set("jorelali")
        name.set("Jorel Ali")
        url.set("https://jorel.dev/")
      }
      developer {
        id.set("DerEchtePilz")
        name.set("DerEchtePilz")
        url.set("https://github.com/DerEchtePilz")
      }
      developer {
        id.set("willkroboth")
        name.set("Will Kroboth")
        url.set("https://github.com/willkroboth")
      }
    }
    scm {
      url.set("https://github.com/CommandAPI/CommandAPI/tree/master")
      connection.set("scm:git:git://github.com/CommandAPI/CommandAPI.git")
      developerConnection.set("scm:git:ssh://github.com:CommandAPI/CommandAPI.git")
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
