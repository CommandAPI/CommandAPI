plugins {
	kotlin("jvm") version "1.8.20"
}

kotlin {
	jvmToolchain(17)
}

version = "0.0.1-SNAPSHOT"

repositories {
	// Use Maven Central for resolving dependencies.
	mavenCentral()
	// This adds the Spigot Maven repository to the build
	maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))

	// This adds the Spigot API artifact to the build
	implementation("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

	// The CommandAPI dependency used for Bukkit and it's forks
	implementation("dev.jorel:commandapi-bukkit-core:10.1.2")
	// Due to all functions available in the kotlindsl being inlined, we only need this dependency at compile-time
	compileOnly("dev.jorel:commandapi-bukkit-kotlin:10.1.2")
}