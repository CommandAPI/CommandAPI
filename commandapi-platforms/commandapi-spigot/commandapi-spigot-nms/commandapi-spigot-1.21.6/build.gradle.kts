plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.6"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1216) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(libs.org.spigotmc.spigot.v1216)

	compileOnly(project(":commandapi-bukkit-1.21.6"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	api(project(":commandapi-spigot-core"))
}