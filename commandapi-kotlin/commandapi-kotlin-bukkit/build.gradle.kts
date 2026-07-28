import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.withType

plugins {
	id("buildlogic.java-conventions")
	kotlin("jvm")
	id("com.gradleup.shadow")
}

description = "Bukkit support in Kotlin"

dependencies {
	compileOnly(paper.version.v1201)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)

	implementation(project(":commandapi-kotlin-core"))
	compileOnly(project(":commandapi-bukkit-core"))
}

kotlin {
	jvmToolchain(17)
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