plugins {
	id("buildlogic.java-conventions")
}

description = "null"

dependencies {
	implementation(libs.dev.jorel.commandapi.core)
	implementation(libs.dev.jorel.commandapi.sponge.core)
	compileOnly(libs.org.spongepowered.spongeapi)
}

java {
	withSourcesJar()
	withJavadocJar()
}