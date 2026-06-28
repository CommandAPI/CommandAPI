plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "Paper support for 26.2"

dependencies {
	paperweight.paperDevBundle("26.2.build.+")

	compileOnly(project(":commandapi-bukkit-26.2"))
	compileOnly(project(":commandapi-bukkit-26-common"))
	compileOnly(project(":commandapi-paper-core"))
}

configurations.create("include") {
	isCanBeConsumed = true
	isCanBeResolved = false
}

artifacts {
	add("include", tasks.jar)
}