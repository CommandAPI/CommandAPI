plugins {
	id("buildlogic.java-conventions")
	kotlin("jvm")
}

description = "CommandAPI - Spigot support tests"

repositories {
	maven {
		url = uri("https://jitpack.io")
		content {
			includeGroup("com.saicone.rtag")
		}
	}
}

/*
   These tests are compiled against 26.3, but they can't run yet: MockBukkit 4 (the only MockBukkit that runs on
   Java 25, which 26.x requires) needs paper-api, while Spigot's 26.x server needs classes that only spigot-api has.
   paper-api 26.x declares the spigot-api capability, and neither classpath order works:
   - paper-api first: Spigot's Bootstrap fails (NoClassDefFoundError for Spigot-only events, or a VerifyError)
   - spigot-api first: MockBukkit fails (NoSuchMethodError for Paper's API, e.g. NamespacedKey#value)
   Until there's a way to run Spigot's server with MockBukkit, the tests are only compiled, so they don't fall behind.
   The Paper tests (commandapi-paper-test-tests) run the shared tests.
*/

dependencies {
	// Dependencies of the shared tests
	testImplementation(libs.com.github.zafarkhaja.java.semver)
	testImplementation(libs.de.tr7zw.item.nbt.api)
	testImplementation(libs.com.saicone.rtag.rtag)
	testImplementation(project(":commandapi-bukkit-plugin-common"))
	// Dependency of the shared Kotlin DSL tests
	testImplementation(project(":commandapi-kotlin-spigot"))

	// MockBukkit 4 uses JUnit 6
	testImplementation(platform("org.junit:junit-bom:6.1.3"))
	testImplementation("org.junit.jupiter:junit-jupiter-engine")
	testImplementation("org.junit.jupiter:junit-jupiter-params")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation(libs.com.mojang.brigadier)
	testImplementation(libs.org.apache.logging.log4j.log4j.api)

	// MockBukkit is compiled against paper-api, so the tests are compiled against Paper's API instead of Spigot's
	testImplementation(spigot.version.v263) {
		exclude(group = "org.spigotmc", module = "spigot-api")
	}
	testImplementation(paper.version.v263)

	// Previously commandapi-bukkit-shade, now the Spigot platform
	testImplementation(project(":commandapi-spigot-core"))
	testImplementation(project(":commandapi-bukkit-core"))
	testImplementation(project(":commandapi-bukkit-26-common"))
	testImplementation(project(":commandapi-bukkit-26.3"))
	testImplementation(project(":commandapi-spigot-26-common"))
	testImplementation(project(":commandapi-spigot-26.3"))

	testImplementation(project(":commandapi-bukkit-test-impl-26.3"))
	testImplementation(project(":commandapi-spigot-test-impl-26.3"))
}

tasks.withType<Test> {
	// See above
	enabled = false
	failOnNoDiscoveredTests = false

	useJUnitPlatform()
	// Every test sets up a new mocked server and registries. Gradle's default 512MB isn't enough for that,
	//  while Maven's Surefire used the JVM's default (a quarter of the system's memory)
	maxHeapSize = "2g"
	testLogging {
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
	}
}

/*
   The tests in commandapi-bukkit-test-tests are shared between the platforms. They need this module's
   CommandAPIVersionHandler and Main (and test resources), which set up the platform.
*/
val sharedTests = objects.sourceDirectorySet("sharedTests", "Tests shared between the Bukkit platforms").apply {
	srcDir(project(":commandapi-bukkit-test-tests").layout.projectDirectory.dir("src/test/java"))
	include("**/*.java")
}

sourceSets.test {
	java.source(sharedTests)
}

/*
   The Kotlin DSL tests in commandapi-bukkit-kotlin-test are shared between the platforms too
*/
kotlin {
	sourceSets.test {
		kotlin.srcDir(project(":commandapi-bukkit-kotlin-test").layout.projectDirectory.dir("src/test/kotlin"))
	}
	compilerOptions {
		// Same as the Java code (see buildlogic.java-conventions). jvmToolchain isn't used, because the
		//  test classpath has classes for newer Java versions.
		jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
	}
}
