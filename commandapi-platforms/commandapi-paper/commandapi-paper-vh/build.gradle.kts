plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support version handler"

dependencies {
	implementation(libs.dev.jorel.commandapi.paper.nms.dependency)
	compileOnly(libs.paper.version.common)
}

java {
	withSourcesJar()
	withJavadocJar()
}