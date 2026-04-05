plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.21.2"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1213) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(libs.org.spigotmc.spigot.v1213)
	compileOnly(libs.io.papermc.paper.paper.api.v1213)

	compileOnly(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}