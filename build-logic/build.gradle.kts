plugins {
	`kotlin-dsl`
}

gradlePlugin {
	plugins {
		create("buildlogic.spigot-remap") {
			id = "buildlogic.spigot-remap"
			implementationClass = "spigotremap.SpigotRemapPlugin"
		}
		create("buildlogic.github-publish") {
			id = "buildlogic.github-publish"
			implementationClass = "publishgithub.GitHubPublishPlugin"
		}
	}
}

dependencies {
	implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)
	implementation("com.vanniktech:gradle-maven-publish-plugin:0.37.0")
	implementation("net.md-5:SpecialSource:1.11.3")
	implementation("com.google.code.gson:gson:2.13.2")
}
