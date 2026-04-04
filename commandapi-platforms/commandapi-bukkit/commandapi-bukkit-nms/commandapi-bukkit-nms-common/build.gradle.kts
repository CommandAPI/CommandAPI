plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support NMS common library"

dependencies {
	compileOnly(libs.spigot.version)
	implementation(libs.dev.jorel.commandapi.bukkit.core)
	compileOnly(libs.paper.version)
}

java {
	withSourcesJar()
	withJavadocJar()
}