plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "Paper support for 26.1"

dependencies {
	paperweight.paperDevBundle("26.1.1.build.+")

	compileOnly(project(":commandapi-bukkit-26.1"))
	compileOnly(project(":commandapi-paper-core"))
}

configurations.create("include") {
	isCanBeConsumed = true
	isCanBeResolved = false
}

artifacts {
	add("include", tasks.jar)
}