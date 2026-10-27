rootProject.name = "build-logic"

dependencyResolutionManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
	}
	versionCatalogs {
		create("libs") {
			from(files("../gradle/libs.versions.toml"))
		}
	}
}
