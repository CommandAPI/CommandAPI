plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":commandapi-core"))
    api(libs.dev.jorel.commandapi.bukkit.core)
    api(libs.dev.jorel.commandapi.spigot.core)
    api(libs.dev.jorel.commandapi.paper.core)
    api(libs.dev.jorel.commandapi.bukkit.test.toolkit)
    api(libs.dev.jorel.commandapi.spigot.test.toolkit)
    api(libs.dev.jorel.commandapi.paper.test.toolkit)
}

description = "CommandAPI - Code Coverage Utility"

java {
    withJavadocJar()
}
