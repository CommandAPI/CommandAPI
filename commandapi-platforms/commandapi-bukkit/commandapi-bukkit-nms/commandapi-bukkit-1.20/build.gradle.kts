plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.20"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1201) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(libs.org.spigotmc.spigot.v1201)
	compileOnly(libs.io.papermc.paper.paper.api.v1201)

	compileOnly(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
}