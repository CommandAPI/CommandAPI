plugins {
	id("buildlogic.java-conventions")
}

description = "Paper Annotations"

dependencies {
	compileOnly(libs.com.google.auto.service.auto.service)
	implementation(libs.dev.jorel.commandapi.paper.core)
	compileOnly(libs.paper.version.common)
	implementation(libs.dev.jorel.commandapi.annotations)
}

java {
	withSourcesJar()
	withJavadocJar()
}