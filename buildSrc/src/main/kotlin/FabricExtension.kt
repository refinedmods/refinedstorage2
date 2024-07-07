import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

open class FabricExtension(private val project: Project) : BaseExtension(project) {
    var modId: String? = null

    fun fabric() {
        project.dependencies.add("minecraft", "com.mojang:minecraft:${mcVersion}")
        project.dependencies.add("mappings", project.extensions.getByType<LoomGradleExtensionAPI>().layered() {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${mcVersion}:${parchmentVersion}@zip")
        })
        project.dependencies.add("modImplementation", "net.fabricmc:fabric-loader:${fabricLoaderVersion}")
        project.dependencies.add("modImplementation", "net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")

        project.extensions.getByType<LoomGradleExtensionAPI>().apply {
            val accessWidenerFile = project.file("src/main/resources/${modId!!}.accesswidener")
            if (accessWidenerFile.exists()) {
                accessWidenerPath.set(accessWidenerFile)
            }
            runs {
                getByName("client") {
                    client()
                    setConfigName("Fabric Client")
                    ideConfigGenerated(true)
                    runDir("run")
                }
                getByName("server") {
                    server()
                    setConfigName("Fabric Server")
                    ideConfigGenerated(true)
                    runDir("run")
                }
            }
            mixin {
                showMessageTypes.set(true)
                messages.put("TARGET_ELEMENT_NOT_FOUND", "disabled")
            }
        }
        project.tasks.withType<Jar>().configureEach {
            from("../LICENSE.md")
        }
    }

    fun addProject(dependency: Project) {
        project.dependencies.add("api", dependency)
        project.dependencies.add("include", dependency)
    }

    fun compileWithProject(dependency: Project) {
        val sourceSets = dependency.extensions.getByType<JavaPluginExtension>().sourceSets
        project.tasks.withType<JavaCompile>().configureEach {
            source(sourceSets["main"].allSource)
        }
        project.tasks.withType<ProcessResources>().configureEach {
            from(sourceSets["main"].resources)
        }
        project.dependencies.add("compileOnly", dependency)
    }
}
