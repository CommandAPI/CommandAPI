package spigotremap

import net.md_5.specialsource.Jar
import net.md_5.specialsource.JarMapping
import net.md_5.specialsource.JarRemapper
import net.md_5.specialsource.provider.JarProvider
import net.md_5.specialsource.provider.JointProvider

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * Runs the <a href=https://github.com/md-5/SpecialSource>SpecialSource</a> JarRemapper.
 * Gradle logic adapted from
 * <a href="https://github.com/typst-io/gradle-specialsource/blob/0bd1af799279e2d3ce56adacca8f89ed77fd67fb/src/main/kotlin/io/typst/gradlesource/RemapTask.kt">io/typst/gradlesource/RemapTask</a>,
 * but updated to include class inheritance information like
 * <a href="https://github.com/agaricusb/SpecialSourceMP/blob/a333c744e9e9b05aa8d567f3f63e19d27b63721f/src/main/java/net/md_5/specialsource/mavenplugin/RemapMojo.java#L205-L211">net.md_5.specialsource.mavenplugin.RemapMojo</a>
 * does.
 */
abstract class SpigotRemapTask : DefaultTask() {
    @get:InputFile
    @get:SkipWhenEmpty
    abstract val inputJarFile: RegularFileProperty

    @get:OutputFile
    abstract val outputJarFile: RegularFileProperty

    @get:InputFile
    abstract val mappingFile: RegularFileProperty

    @get:InputFiles
    @get:Optional
    abstract val inheritanceJars: ConfigurableFileCollection

    @get:Input
    @get:Optional
    abstract val reverse: Property<Boolean>

    @TaskAction
    fun remap() {
		val mapping = JarMapping()
		mapping.loadMappings(
			mappingFile.asFile.get().absolutePath,
			reverse.getOrElse(false),
			false, null, null
		)

		val provider = JointProvider()
		val openInheritanceJars = mutableListOf<Jar>()

		try {
			Jar.init(inputJarFile.asFile.get()).use { inputJar ->
				provider.add(JarProvider(inputJar))

				inheritanceJars.files
					.filter { it.exists() && it.isFile }
					.forEach { inheritanceFile ->
						val inheritance = Jar.init(inheritanceFile)
						openInheritanceJars.add(inheritance)
						provider.add(JarProvider(inheritance))
					}

				mapping.setFallbackInheritanceProvider(provider)

				val remapper = JarRemapper(null, mapping, null)
				remapper.remapJar(inputJar, outputJarFile.get().asFile)
			}
		} finally {
			openInheritanceJars.forEach { it.close() }
		}
    }
}
