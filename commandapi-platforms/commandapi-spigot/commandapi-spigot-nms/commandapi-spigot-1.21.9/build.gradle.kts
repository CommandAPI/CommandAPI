plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.9"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1219) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(libs.org.spigotmc.spigot.v1219)

	compileOnly(project(":commandapi-bukkit-1.21.9"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	api(project(":commandapi-spigot-core"))
}