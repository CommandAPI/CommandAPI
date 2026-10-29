import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
	id("com.gradleup.shadow")
}

description = "CommandAPI - Paper support test library implementation for 1.20.5"

dependencies {
	paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")

	api(project(":commandapi-bukkit-test-impl"))
	api(libs.org.mockito.mockito.core)
	api("com.github.seeseemelk:MockBukkit-v1.20:3.93.2")

	compileOnly(project(":commandapi-paper-core"))
	compileOnly(project(":commandapi-paper-1.20.5", "include"))
	compileOnly(project(":commandapi-bukkit-nms-common", "mojang"))
	compileOnly(project(":commandapi-bukkit-1.20.5", "mojang"))
	compileOnly(project(":commandapi-bukkit-test-impl-1.20.5", "mojang"))

	// The Bukkit NMS and test implementations are compiled against Spigot, which uses a versioned CraftBukkit
	// package. Paper doesn't, so we bundle them with the CraftBukkit package relocated like commandapi-paper-shade
	shadow(project(":commandapi-bukkit-1.20.5", "mojang")) {
		isTransitive = false
	}
	shadow(project(":commandapi-bukkit-test-impl-1.20.5", "mojang")) {
		isTransitive = false
	}
}

shadow {
	addShadowVariantIntoJavaComponent = false
}

tasks.withType<Jar> {
	archiveClassifier = "thin"
}

tasks.withType<ShadowJar> {
	archiveClassifier = ""
	configurations = listOf(project.configurations["shadow"])

	relocate("org.bukkit.craftbukkit.v1_20_R4", "org.bukkit.craftbukkit")
}

afterEvaluate {
	configurations["shadowRuntimeElements"].isCanBeConsumed = false
	configurations["shadow"].isCanBeConsumed = false
}

// Like commandapi-paper-1.20.5, consumers need to request this configuration explicitly because
// paperweight's configurations make the default variant ambiguous
configurations.create("include") {
	isCanBeConsumed = true
	isCanBeResolved = false
	extendsFrom(configurations.implementation.get(), configurations.runtimeOnly.get())
}

artifacts {
	add("include", tasks.shadowJar)
}
