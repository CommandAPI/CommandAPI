plugins {
	id("buildlogic.java-conventions")
}

description = "Bukkit support for 1.21.9"

dependencies {
	compileOnly(libs.org.spigotmc.spigot.v1219) {
		artifact {
			classifier = "remapped-mojang"
		}
	}
	testImplementation(libs.org.spigotmc.spigot.v1219)

	compileOnly(project(":commandapi-bukkit-core"))
	compileOnly(project(":commandapi-bukkit-nms-common"))
	compileOnly(project(":commandapi-preprocessor"))
	annotationProcessor(project(":commandapi-preprocessor"))
	testCompileOnly(project(":commandapi-preprocessor"))
	testAnnotationProcessor(project(":commandapi-preprocessor"))
}

tasks.withType<Test> {
	failOnNoDiscoveredTests = false
}