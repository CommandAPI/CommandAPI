import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.withType

plugins {
	id("buildlogic.java-conventions")
	kotlin("jvm")
	id("com.gradleup.shadow")
}

description = "Spigot support in Kotlin"

dependencies {
	compileOnly(spigot.version.api)
	compileOnly(libs.org.jetbrains.kotlin.kotlin.stdlib)

	implementation(project(":commandapi-kotlin-bukkit"))
	compileOnly(project(":commandapi-spigot-core"))
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