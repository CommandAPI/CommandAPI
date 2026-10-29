import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
plugins {
	id("buildlogic.java-conventions")
	id("com.gradleup.shadow")
	id("buildlogic.publishing-conventions")
}

description = "Spigot support testing toolkit"

dependencies {
	compileOnly(paper.version.v1218)
	compileOnly(libs.org.mockbukkit.mockbukkit.mockbukkit.v121)

	shadow(project(":commandapi-bukkit-test-toolkit"))
	compileOnly(project(":commandapi-spigot-core"))

	testImplementation(paper.version.v1218)
	testImplementation(libs.org.mockbukkit.mockbukkit.mockbukkit.v121)
	testImplementation(project(":commandapi-bukkit-test-toolkit"))
	testImplementation(project(":commandapi-spigot-core"))

	testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
	testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

shadow {
	addShadowVariantIntoJavaComponent = false
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("test") {
	dependsOn(tasks.shadowJar)
}

tasks.withType<Jar> {
	archiveClassifier = "thin"
}

tasks.withType<ShadowJar> {
	archiveClassifier = ""
	configurations = listOf(project.configurations["shadow"])
}

afterEvaluate {
	configurations["shadowRuntimeElements"].isCanBeConsumed = false
	configurations["shadow"].isCanBeConsumed = false
}
