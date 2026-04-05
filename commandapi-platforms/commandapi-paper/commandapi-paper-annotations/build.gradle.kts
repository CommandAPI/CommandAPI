plugins {
	id("buildlogic.java-conventions")
}

description = "Paper Annotations"

dependencies {
	compileOnly(libs.com.google.auto.service.auto.service)
	compileOnly(libs.paper.version.common)

	compileOnly(project(":commandapi-annotations"))
	compileOnly(project(":commandapi-paper-core"))
}