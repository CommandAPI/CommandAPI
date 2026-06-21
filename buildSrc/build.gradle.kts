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
    }
}

dependencies {
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.4.1")
    implementation("net.md-5:SpecialSource:1.11.3")
}
