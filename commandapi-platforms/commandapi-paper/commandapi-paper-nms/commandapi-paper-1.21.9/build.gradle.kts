plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "Paper support for 1.21.9"

dependencies {
	paperweight.paperDevBundle("1.21.9-R0.1-SNAPSHOT")

	compileOnly(project(":commandapi-bukkit-1.21.9"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-paper-core"))
}

configurations.create("include") {
	isCanBeConsumed = true
	isCanBeResolved = false
}

artifacts {
	add("include", tasks.jar)
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}