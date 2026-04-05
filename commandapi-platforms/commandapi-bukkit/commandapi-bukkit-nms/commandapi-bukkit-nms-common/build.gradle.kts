plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support NMS common library"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1201) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	compileOnly(libs.paper.version.bukkit.common)

	compileOnly(project(":commandapi-bukkit-core"))
}