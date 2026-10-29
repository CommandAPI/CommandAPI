plugins {
	id("buildlogic.java-conventions")
	id("buildlogic.publishing-conventions")
}

description = "Spigot Annotations"

dependencies {
	compileOnly(libs.com.google.auto.service.auto.service)
	annotationProcessor(libs.com.google.auto.service.auto.service)
	compileOnly(spigot.version.api)

	api(project(":commandapi-annotations"))
	api(project(":commandapi-spigot-core"))
}
