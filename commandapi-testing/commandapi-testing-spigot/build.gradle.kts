plugins {
	id("buildlogic.java-conventions")
}

description = "Testing plugin for Spigot"

val commandApiShade = project(":commandapi-spigot-shade")

evaluationDependsOn(":commandapi-spigot-shade")

dependencies {
	compileOnly(libs.com.mojang.brigadier)
	compileOnly(spigot.version.api)

	implementation(files(commandApiShade.tasks.named("shadowJar")))
}