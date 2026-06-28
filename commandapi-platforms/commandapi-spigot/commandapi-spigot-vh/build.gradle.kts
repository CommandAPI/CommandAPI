plugins {
	id("buildlogic.java-conventions")
}

description = "Spigot support version handler"

dependencies {
	compileOnly(spigot.version.common)
	compileOnly(spigot.version.common) {
		artifact {
			classifier = "remapped-mojang"
		}
	}

	compileOnly(project(":commandapi-spigot-nms-dependency"))
}