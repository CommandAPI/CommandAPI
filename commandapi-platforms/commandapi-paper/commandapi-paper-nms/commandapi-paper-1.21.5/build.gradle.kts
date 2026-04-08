plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "Paper support for 1.21.5"

dependencies {
	paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")

	compileOnly(project(":commandapi-bukkit-1.21.5"))
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