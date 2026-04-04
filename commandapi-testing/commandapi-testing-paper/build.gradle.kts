plugins {
	id("buildlogic.java-conventions")
}

description = "Testing plugin for Paper"

dependencies {
	compileOnly(libs.paper.version.v1206)
	implementation(libs.dev.jorel.commandapi.paper.shade)
}

java {
	withSourcesJar()
	withJavadocJar()
}