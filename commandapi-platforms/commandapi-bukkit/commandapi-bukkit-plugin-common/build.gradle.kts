plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support plugin common library"

dependencies {
	implementation(libs.dev.jorel.commandapi.plugin)
	compileOnly(libs.paper.version.v1211)
	compileOnly(libs.dev.jorel.commandapi.bukkit.core)
}

java {
	withSourcesJar()
	withJavadocJar()
}