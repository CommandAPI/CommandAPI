plugins {
	id("buildlogic.java-conventions")
}

description = "null"

dependencies {
	compileOnly(libs.spigot.version.v1201)
	implementation(libs.dev.jorel.commandapi.bukkit.shade)
	testImplementation(libs.org.mockito.mockito.core)
	implementation(libs.paper.version.v1201)
	testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
	implementation(libs.org.jetbrains.kotlin.kotlin.stdlib)
	implementation(libs.dev.jorel.commandapi.bukkit.kotlin)
	testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
	testImplementation(libs.dev.jorel.commandapi.bukkit.test.impl.v120)
	implementation(libs.com.github.seeseemelk.MockBukkit.v120)
	implementation(libs.com.mojang.brigadier)
}

java {
	withSourcesJar()
	withJavadocJar()
}