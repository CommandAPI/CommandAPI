import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.withType

plugins {
	id("buildlogic.java-conventions")
}

description = "Velocity support plugin"

dependencies {
	compileOnly(paper.velocity.api)
	annotationProcessor(paper.velocity.api)

	implementation(project(":commandapi-velocity-core"))
	implementation(project(":commandapi-plugin"))
}

tasks.withType<ProcessResources> {
}