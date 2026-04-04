plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support version handler"

dependencies {
	implementation(libs.dev.jorel.commandapi.spigot.nms.dependency)
	compileOnly(libs.spigot.version.common)
}

java {
	withSourcesJar()
	withJavadocJar()
}