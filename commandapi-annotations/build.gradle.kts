plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    compileOnly(libs.dev.jorel.commandapi.bukkit.core)
    compileOnly(libs.com.google.auto.service.auto.service)
    compileOnly(libs.org.spigotmc.spigot.api)
}

description = "Annotations Library"

java {
    withJavadocJar()
}
