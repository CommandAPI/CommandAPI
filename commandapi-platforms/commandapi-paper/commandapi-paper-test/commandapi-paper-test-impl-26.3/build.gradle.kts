plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
}

description = "CommandAPI - Paper support test library implementation for 26.3"

dependencies {
	paperweight.paperDevBundle(paper.versions.paper.api.v263)

	// Since 26.1, CraftBukkit's package isn't versioned on Spigot either, so unlike for 1.20.5, the Bukkit test
	//  implementation doesn't need to be bundled with the CraftBukkit package relocated
	api(project(":commandapi-bukkit-test-impl-26.3"))

	compileOnly(project(":commandapi-paper-core"))
	compileOnly(project(":commandapi-paper-26.3", "include"))
	compileOnly(project(":commandapi-bukkit-26-common"))
	compileOnly(project(":commandapi-bukkit-26.3"))
}

// Like commandapi-paper-26.3, consumers need to request this configuration explicitly because
// paperweight's configurations make the default variant ambiguous
configurations.create("include") {
	isCanBeConsumed = true
	isCanBeResolved = false
	extendsFrom(configurations.implementation.get(), configurations.runtimeOnly.get())
}

artifacts {
	add("include", tasks.jar)
}
