package spigotremap

import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.bundling.AbstractArchiveTask

abstract class SpigotRemapConfiguration {
    abstract val sourceJarTask: Property<AbstractArchiveTask>

    abstract val spigotVersion: Property<String>

    val spigotVersionExact: Provider<String> = spigotVersion.map { ver ->
        val pieces = ver.split("-")
        val r = pieces.getOrNull(1) ?: "R0.1"
        val tag = pieces.getOrNull(2) ?: "SNAPSHOT"
        "${pieces[0]}-${r}-${tag}"
    }
}
