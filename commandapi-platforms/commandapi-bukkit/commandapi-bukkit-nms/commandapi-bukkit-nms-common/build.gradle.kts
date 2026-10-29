plugins {
	id("buildlogic.java-conventions")
	id("buildlogic.spigot-remap")
}

description = "Bukkit support NMS common library"

// Optionally compile against a specific Minecraft version (used by compileNMSCommon.sh), e.g. -PnmsCommonVersion=1.21.11
// The output of such a build is placed in build/<version> so builds for different versions don't overwrite each other
val nmsCommonVersions = mapOf(
	"1.20.1" to spigot.version.v1201,
	"1.20.2" to spigot.version.v1202,
	"1.20.4" to spigot.version.v1204,
	"1.20.6" to spigot.version.v1206,
	"1.21.1" to spigot.version.v1211,
	"1.21.3" to spigot.version.v1213,
	"1.21.4" to spigot.version.v1214,
	"1.21.5" to spigot.version.v1215,
	"1.21.6" to spigot.version.v1216,
	"1.21.9" to spigot.version.v1219,
	"1.21.11" to spigot.version.v12111,
)
val nmsCommonVersion: String? = providers.gradleProperty("nmsCommonVersion").orNull

if (nmsCommonVersion != null) {
	require(nmsCommonVersion in nmsCommonVersions) {
		"Unknown nmsCommonVersion: $nmsCommonVersion! Please choose one of ${nmsCommonVersions.keys}"
	}
	layout.buildDirectory = layout.projectDirectory.dir("build/$nmsCommonVersion")

	// Newer paper-api versions declare the spigot-api capability, which conflicts with spigot's transitive spigot-api
	configurations.compileClasspath {
		resolutionStrategy.capabilitiesResolution.withCapability("org.spigotmc:spigot-api") {
			select(candidates.first { (it.id as? ModuleComponentIdentifier)?.module == "paper-api" })
		}
	}
}

dependencies {
	compileOnly(if (nmsCommonVersion != null) nmsCommonVersions.getValue(nmsCommonVersion) else spigot.version.common) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	if (nmsCommonVersion != null) {
		compileOnly("io.papermc.paper:paper-api:$nmsCommonVersion-R0.1-SNAPSHOT")
	} else {
		compileOnly(paper.version.bukkit.common)
	}

	compileOnly(project(":commandapi-bukkit-core"))
}

val mappingAttribute = Attribute.of("mappingType", String::class.java)

configurations.create("spigot") {
	isCanBeConsumed = true
	isCanBeResolved = false
	attributes {
		attribute(mappingAttribute, "spigot")
	}
}

configurations.create("mojang") {
	isCanBeConsumed = true
	isCanBeResolved = false
	attributes {
		attribute(mappingAttribute, "mojang")
	}
}

artifacts {
	add("spigot", tasks.remapObfToSpigot.flatMap { it.outputJarFile })
	add("mojang", tasks.jar)
}

spigotRemap {
	spigotVersion = nmsCommonVersion ?: "1.20.1"
	sourceJarTask = tasks.jar
}
