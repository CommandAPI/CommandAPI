plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support testing toolkit"

dependencies {
	compileOnly(libs.org.jetbrains.annotations)
	compileOnly(libs.paper.version.v1218)
	implementation(libs.org.mockito.mockito.core)
	testImplementation(libs.org.junit.jupiter.junit.jupiter.params)
	implementation(libs.org.mockbukkit.mockbukkit.mockbukkit.v121)
	testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
	implementation(libs.com.mojang.brigadier)
	implementation(libs.org.slf4j.slf4j.nop)

	compileOnly(project(":commandapi-bukkit-core"))
}