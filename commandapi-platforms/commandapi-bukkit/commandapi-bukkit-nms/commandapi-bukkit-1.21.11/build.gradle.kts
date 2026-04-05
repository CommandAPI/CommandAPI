plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.21.11"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v12111) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(libs.org.spigotmc.spigot.v12111)
	compileOnly(libs.io.papermc.paper.paper.api.v12111)

	compileOnly(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}