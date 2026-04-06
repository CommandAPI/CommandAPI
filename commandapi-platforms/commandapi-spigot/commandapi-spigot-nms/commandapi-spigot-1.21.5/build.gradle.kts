plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.5"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1215) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(libs.org.spigotmc.spigot.v1215)

	compileOnly(project(":commandapi-bukkit-1.21.5"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	api(project(":commandapi-spigot-core"))
}