plugins {
	id("buildlogic.java-conventions")
}

description = "null"

dependencies {
	implementation(libs.dev.jorel.commandapi.core)
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(libs.com.mojang.authlib)
	compileOnly(libs.org.spongepowered.spongeapi)
	compileOnly(libs.dev.jorel.commandapi.preprocessor)
}