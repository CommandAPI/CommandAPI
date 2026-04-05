plugins {
	id("buildlogic.java-conventions")
}

description = "null"

dependencies {
	implementation(libs.org.mockito.mockito.core)
	implementation(libs.dev.jorel.commandapi.bukkit.test.impl)
	implementation(libs.dev.jorel.commandapi.bukkit.plugin.common)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.de.tr7zw.item.nbt.api)
	compileOnly(libs.com.saicone.rtag.rtag)
	testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
	implementation(libs.com.github.zafarkhaja.java.semver)
}