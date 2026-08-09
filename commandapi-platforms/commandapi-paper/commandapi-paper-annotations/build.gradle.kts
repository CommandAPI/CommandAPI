import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.withType

plugins {
	id("buildlogic.java-conventions")
	id("com.gradleup.shadow")
}

description = "Paper Annotations"

dependencies {
	compileOnly(libs.com.google.auto.service.auto.service)
	annotationProcessor(libs.com.google.auto.service.auto.service)
	compileOnly(paper.version.common)

	implementation(project(":commandapi-annotations"))
	compileOnly(project(":commandapi-paper-core"))

	testCompileOnly(paper.version.common)
	testCompileOnly(project(":commandapi-annotations"))
	testCompileOnly(project(":commandapi-paper-core"))
}

tasks.withType<Test> {
	failOnNoDiscoveredTests = false
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