package spigotremap

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class SpigotRemapPlugin : Plugin<Project> {
    private fun TaskProvider<SpigotRemapTask>.configureRemapTask(
		p: Project,
		inputJar: Provider<RegularFile>,
		outputJar: Provider<RegularFile>,
		version: Provider<String>,
		mappings: String,
		reverse: Boolean = false
    ) {
        configure {
			// Input jar that we need to remap
            inputJarFile.set(inputJar)
			// Resulting jar for the output
            outputJarFile.set(outputJar)
			// Mapping information for this step
            mappingFile.set(p.layout.file(
                p.provider {
                    p.configurations.detachedConfiguration(
                        p.dependencies.create(
							"org.spigotmc:minecraft-server:${version.get()}:${mappings}"
						)
                    ).singleFile
                }
            ))
			// Give access to the compileClasspath for dependency information
            inheritanceJars.from(
				p.configurations.findByName("compileClasspath") ?: p.objects.fileCollection()
			)
            this.reverse.set(reverse)
        }
    }

    override fun apply(project: Project) {
        project.pluginManager.apply(JavaPlugin::class)
        project.repositories.mavenLocal {
            metadataSources {
                mavenPom() // To resolve `maven-metadata-local.xml`
                artifact()
            }
        }

		// Read configuration
        val configuration = project.extensions.create<SpigotRemapConfiguration>("spigotRemap")
		val version = configuration.spigotVersionExact
		val outputJar = { classifier: String ->
			configuration.sourceJarTask.flatMap { jarTask ->
				jarTask.destinationDirectory.map { dir ->
					// Place output file in same location as source file
					dir.file("${jarTask.archiveBaseName.get()}-${jarTask.archiveVersion.get()}-${classifier}.jar")
				}
			}
		}

		// Create tasks
        val remapMojangToObf = project.tasks.register<SpigotRemapTask>("remapMojangToObf")
		remapMojangToObf.configureRemapTask(
			project,
			// Input from the source jar
			configuration.sourceJarTask.flatMap { it.archiveFile },
			outputJar("obf"),
			version,
			"maps-mojang@txt",
			reverse = true
		)

        val remapObfToSpigot = project.tasks.register<SpigotRemapTask>("remapObfToSpigot")
        remapObfToSpigot.configureRemapTask(
            project,
			// Input from the previous step
            remapMojangToObf.flatMap { it.outputJarFile },
            outputJar("spigot"),
			version,
            "maps-spigot@csrg"
        )

		// These tasks create jar files
        val assemble = project.tasks.getByName("assemble")
        assemble.dependsOn(remapObfToSpigot)
    }
}
