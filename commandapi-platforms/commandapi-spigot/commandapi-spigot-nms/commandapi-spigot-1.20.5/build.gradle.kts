plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support for 1.20.5"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1206) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(libs.org.spigotmc.spigot.v1206)

	compileOnly(project(":commandapi-bukkit-1.20.5"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	api(project(":commandapi-spigot-core"))
}