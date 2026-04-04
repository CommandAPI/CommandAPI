plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    compileOnly(libs.com.mojang.brigadier)
    compileOnly(libs.com.mojang.authlib)
    compileOnly(libs.org.apache.logging.log4j.log4j.api)
    compileOnly(libs.org.jetbrains.annotations)
    annotationProcessor(project(":commandapi-preprocessor"))
}

description = "Core library"

java {
    withJavadocJar()
}
