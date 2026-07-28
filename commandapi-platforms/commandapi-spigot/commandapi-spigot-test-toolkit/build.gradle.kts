import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.withType

plugins {
	id("buildlogic.java-conventions")
	id("com.gradleup.shadow")
}

description = "Spigot support testing toolkit"

dependencies {
	compileOnly(paper.version.v1218)
	compileOnly(libs.org.mockbukkit.mockbukkit.mockbukkit.v121)

	implementation(project(":commandapi-bukkit-test-toolkit"))
	compileOnly(project(":commandapi-spigot-core"))

	testImplementation(paper.version.v1218)
	testImplementation(libs.org.mockbukkit.mockbukkit.mockbukkit.v121)
	testImplementation(project(":commandapi-bukkit-test-toolkit"))
	testImplementation(project(":commandapi-spigot-core"))

	testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
	testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<ShadowJar> {
	dependencies {
		exclude(dependency(libs.org.mockbukkit.mockbukkit.mockbukkit.v121))
	}
	minimize()
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