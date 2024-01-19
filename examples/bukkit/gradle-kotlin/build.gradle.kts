plugins {
	java
}

java {
	toolchain {
		// Make the compiler against Java 17
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

version = "0.0.1-SNAPSHOT"

repositories {
	// Use Maven Central for resolving dependencies.
	mavenCentral()
	// This adds the Paper Maven repository to the build
	maven("https://papermc.io/repo/repository/maven-public/")
	// CodeMC repository for the NBT API
	maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
	// This adds the Paper API artifact to the build
	implementation("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

	// The CommandAPI dependency used for Bukkit and it's forks
	implementation("dev.jorel:commandapi-paper-plugin:10.2.0-SNAPSHOT")

	// NBT API to use NBT-based arguments
	implementation("de.tr7zw:item-nbt-api-plugin:2.12.2")
}