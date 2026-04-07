plugins {
	id("buildlogic.java-conventions")
}

description = "Paper Annotations"

dependencies {
	compileOnly(libs.com.google.auto.service.auto.service)
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