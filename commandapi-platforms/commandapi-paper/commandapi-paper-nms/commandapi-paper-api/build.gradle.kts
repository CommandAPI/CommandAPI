plugins {
	id("buildlogic.java-conventions")
}

description = "Paper API support"

dependencies {
	compileOnly(libs.paper.version.v261)
	compileOnly(libs.dev.jorel.commandapi.paper.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}