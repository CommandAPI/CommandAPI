plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
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
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.3.20")
    implementation("net.md-5:SpecialSource:1.11.3")
}
