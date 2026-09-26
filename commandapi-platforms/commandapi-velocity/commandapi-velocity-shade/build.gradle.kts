import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.withType

plugins {
	id("buildlogic.java-conventions")
	id("com.gradleup.shadow")
	id("com.vanniktech.maven.publish")
}

description = "Velocity support shade library"

dependencies {
	implementation(project(":commandapi-velocity-core"))
}

tasks.named("build") {
	dependsOn(tasks.shadowJar)
}

tasks.named("test") {
	dependsOn(tasks.shadowJar)
}

tasks.withType<Jar> {
	archiveClassifier = "thin"
}

tasks.withType<ShadowJar> {
	archiveClassifier = ""
}

afterEvaluate {
	configurations["shadowRuntimeElements"].isCanBeConsumed = false;
	configurations["shadow"].isCanBeConsumed = false
}

// https://vanniktech.github.io/gradle-maven-publish-plugin/central/
mavenPublishing {
	publishToMavenCentral()

	if (!version.toString().endsWith("SNAPSHOT")) {
		// Don't sign SNAPSHOT versions, only main releases
		signAllPublications()
	}

	coordinates(group.toString(), name, version.toString())

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