plugins {
	id("buildlogic.java-conventions")
}

description = "Development Maven preprocessor"

dependencies {
}

java {
	withSourcesJar()
	withJavadocJar()
}