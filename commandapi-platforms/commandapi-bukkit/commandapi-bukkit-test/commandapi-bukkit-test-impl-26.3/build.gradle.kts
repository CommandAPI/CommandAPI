plugins {
	id("buildlogic.java-conventions")
}

description = "CommandAPI - Bukkit support test library implementation for 26.3"

dependencies {
	// Since 26.1, Minecraft isn't obfuscated anymore, so there's no Spigot/Mojang mapped variant to choose from.
	//  MockBukkit is compiled against paper-api, so javac needs Paper's API to read MockBukkit's classes. paper-api
	//  replaces spigot-api here, because paper-api 26.x declares the spigot-api capability.
	compileOnly(spigot.version.v263) {
		exclude(group = "org.spigotmc", module = "spigot-api")
	}
	compileOnly(paper.version.v263)
	compileOnly(libs.com.mojang.brigadier)

	// MockBukkit-v1.20 (be.seeseemelk) doesn't run on Java 25, which 26.x requires, so this uses MockBukkit 4.
	//  There isn't a MockBukkit for 26.3 yet, so this is the one for 26.2. Unlike MockBukkit-v1.20, MockBukkit 4
	//  doesn't bring paper-api with it. Consumers provide the Bukkit API, because paper-api 26.x declares the
	//  spigot-api capability and can't be on the same classpath as Spigot's own API.
	// The Mockito version from libs.versions.toml can't mock Java 25 classes
	api("org.mockito:mockito-core:5.20.0")
	api("org.mockbukkit.mockbukkit:mockbukkit-v26.2:4.117.0")
	api(project(":commandapi-bukkit-test-impl"))

	// Previously commandapi-bukkit-shade
	compileOnly(project(":commandapi-bukkit-26-common"))
	compileOnly(project(":commandapi-bukkit-26.3"))
}
