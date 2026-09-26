plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "Paper support for 26.3"

dependencies {
	paperweight.paperDevBundle(paper.versions.paper.api.v263)

	compileOnly(project(":commandapi-bukkit-26.3"))
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