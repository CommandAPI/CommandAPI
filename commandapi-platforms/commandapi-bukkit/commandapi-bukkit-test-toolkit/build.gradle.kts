import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support testing toolkit"

dependencies {
	compileOnly(libs.org.jetbrains.annotations)
	compileOnly(paper.version.v1218)
	compileOnly(libs.org.junit.jupiter.junit.jupiter.engine)
	implementation(libs.org.mockito.mockito.core)
	implementation(libs.org.mockbukkit.mockbukkit.mockbukkit.v121)
	implementation(libs.com.mojang.brigadier)
	implementation(libs.org.slf4j.slf4j.nop)

	compileOnly(project(":commandapi-bukkit-core"))

	testImplementation(paper.version.v1218)
	testImplementation(libs.org.mockbukkit.mockbukkit.mockbukkit.v121)
	testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
	testImplementation(libs.org.junit.jupiter.junit.jupiter.params)
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	testImplementation(project(":commandapi-bukkit-core"))
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	options.release = 21
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<ShadowJar> {
	dependencies {
		exclude(dependency(libs.org.mockbukkit.mockbukkit.mockbukkit.v121))
	}
}