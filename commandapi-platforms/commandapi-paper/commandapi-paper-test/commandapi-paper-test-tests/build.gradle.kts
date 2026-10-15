plugins {
	id("buildlogic.java-conventions")
	id("io.papermc.paperweight.userdev")
	kotlin("jvm")
}

description = "CommandAPI - Paper support tests"

repositories {
	maven {
		url = uri("https://jitpack.io")
		content {
			includeGroup("com.saicone.rtag")
		}
	}
}

dependencies {
	// Dependencies of the shared tests
	testImplementation(libs.com.github.zafarkhaja.java.semver)
	testImplementation(libs.de.tr7zw.item.nbt.api)
	testImplementation(libs.com.saicone.rtag.rtag)
	testImplementation(libs.org.junit.jupiter.junit.jupiter.params)
	testImplementation(project(":commandapi-bukkit-plugin-common"))
	// Dependency of the shared Kotlin DSL tests
	testImplementation(project(":commandapi-kotlin-paper"))

	// Adds the Paper server to the test classpath
	paperweight.paperDevBundle(paper.versions.paper.api.v263)

	// `mojang-mapped` must be before `core` so we resolve the correct version of `MojangMappedVersionHandler`
	testImplementation(project(":commandapi-paper-mojang-mapped"))
	testImplementation(project(":commandapi-paper-core"))
	testImplementation(project(":commandapi-paper-api"))
	testImplementation(project(":commandapi-paper-26.3", "include"))
	testImplementation(project(":commandapi-bukkit-26-common"))
	testImplementation(project(":commandapi-bukkit-26.3"))
	testImplementation(project(":commandapi-paper-test-impl-26.3", "include"))

	// MockBukkit 4 uses JUnit 6
	testImplementation(platform("org.junit:junit-bom:6.1.3"))
	testImplementation("org.junit.jupiter:junit-jupiter-engine")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

interface InjectedArchiveOperations {
	@get:Inject
	val archives: ArchiveOperations
}

/**
 * Registers a task that copies the jar called [jarName] from the test runtime classpath without the given services
 */
fun registerServiceFilteredJar(taskName: String, jarName: String, services: List<String>) = tasks.register<Jar>(taskName) {
	description = "Copies $jarName without the services that conflict between MockBukkit and the Paper server"
	archiveFileName = jarName
	destinationDirectory = layout.buildDirectory.dir("serviceFilteredJars")

	val archives = objects.newInstance<InjectedArchiveOperations>().archives
	val jar = configurations.testRuntimeClasspath.get().filter { it.name == jarName }
	from(jar.elements.map { jars -> jars.map { archives.zipTree(it) } }) {
		exclude(services.map { "META-INF/services/$it" })
	}
}

// MockBukkit and the Paper server both provide implementations of these services, but Paper only accepts one
// provider for each. MockBukkit is the running server here, so we keep its providers for the server itself.
// Paper's providers are kept where they're backed by the (real) Minecraft registries and argument types the
// CommandAPI converts from: registries, argument types, data components and Paper's internal API bridge.
val filteredJars = listOf(
	registerServiceFilteredJar("filterPaperServerServices", "mappedServerJar.jar", listOf(
		"io.papermc.paper.ServerBuildInfo",
		"io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventTypeProvider",
		"net.kyori.adventure.bossbar.BossBarImplementation\$Provider",
		"net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer\$Provider",
		"net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer\$Provider",
		"org.bukkit.plugin.PluginLoader",
	)),
	registerServiceFilteredJar("filterMockBukkitServices", "mockbukkit-v26.2-4.117.0.jar", listOf(
		"io.papermc.paper.InternalAPIBridge",
		"io.papermc.paper.command.brigadier.MessageComponentSerializer",
		"io.papermc.paper.command.brigadier.argument.VanillaArgumentProvider",
		"io.papermc.paper.datacomponent.item.ItemComponentTypesBridge",
		"io.papermc.paper.datacomponent.item.attribute.AttributeModifierDisplayBridge",
		"io.papermc.paper.datacomponent.item.blocksattacks.BlocksAttacksBridge",
		"io.papermc.paper.registry.RegistryAccess",
	)),
)

tasks.named<Test>("test") {
	// Replace the filtered jars in place, so the classpath order stays the same
	val originalClasspath = classpath
	val filteredFiles = files(filteredJars)
	classpath = files(originalClasspath.elements.zip(filteredFiles.elements) { entries, filtered ->
		val byName = filtered.associateBy { it.asFile.name }
		entries.map { (byName[it.asFile.name] ?: it).asFile }
	})
}

tasks.withType<Test> {
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
