plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.21.11"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v12111) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(libs.org.spigotmc.spigot.v12111)

	compileOnly(project(":commandapi-bukkit-1.21.11"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	api(project(":commandapi-spigot-core"))
}