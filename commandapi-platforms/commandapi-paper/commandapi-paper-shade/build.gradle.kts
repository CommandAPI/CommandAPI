import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
	id("com.gradleup.shadow")
	id("com.vanniktech.maven.publish")
}

description = "Paper support shade library"

dependencies {
	// `vh` must be before `core` so we resolve the correct version of `CommandAPIVersionHandler`
	api(project(":commandapi-paper-vh"))
	// `mojang-mapped` must be before `core` so we resolve the correct version of `MojangMappedVersionHandler`
	api(project(":commandapi-paper-mojang-mapped"))
	api(project(":commandapi-paper-core"))
	api(project(":commandapi-paper-nms-dependency"))
}

tasks.withType<ShadowJar> {
	relocate("org.bukkit.craftbukkit.v1_20_R4", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R1", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R2", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R3", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R4", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R5", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R6", "org.bukkit.craftbukkit")
	relocate("org.bukkit.craftbukkit.v1_21_R7", "org.bukkit.craftbukkit")
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