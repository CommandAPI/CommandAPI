plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support testing toolkit"

dependencies {
	compileOnly(libs.paper.version.v1218)
	implementation(libs.org.mockbukkit.mockbukkit.mockbukkit.v121)

	compileOnly(project(":commandapi-bukkit-test-toolkit"))
	compileOnly(project(":commandapi-paper-core"))
}