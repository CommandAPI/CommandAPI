import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
plugins {
	id("buildlogic.java-conventions")
	id("com.gradleup.shadow")
	id("buildlogic.publishing-conventions")
}

description = "Velocity support shade library"

dependencies {
	implementation(project(":commandapi-velocity-core"))
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
	configurations["shadowRuntimeElements"].isCanBeConsumed = false
	configurations["shadow"].isCanBeConsumed = false
}
