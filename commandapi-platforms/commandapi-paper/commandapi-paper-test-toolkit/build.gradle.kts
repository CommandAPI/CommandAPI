plugins {
	id("buildlogic.java-conventions")
}

description = "Paper support testing toolkit"

dependencies {
	compileOnly(libs.paper.version.v1218)
	implementation(libs.org.mockbukkit.mockbukkit.mockbukkit.v121)

	compileOnly(project(":commandapi-bukkit-test-toolkit"))
	compileOnly(project(":commandapi-paper-core"))

	testImplementation(libs.paper.version.v1218)
	testImplementation(project(":commandapi-bukkit-test-toolkit"))
	testImplementation(project(":commandapi-paper-core"))

	testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
	testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}