plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support version handler"

dependencies {
	compileOnly(libs.spigot.version.common)
	compileOnly(libs.spigot.version.common) {
		artifact {
			classifier = "remapped-mojang"
		}
	}

	compileOnly(project(":commandapi-spigot-nms-dependency"))
}