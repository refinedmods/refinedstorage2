import net.neoforged.moddevgradle.dsl.NeoForgeExtension as NfExtension
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

open class NeoForgeExtension(private val project: Project) : BaseExtension(project) {
    var modId: String? = null

    fun neoForge() {
        val sourceSets = project.extensions.getByType<JavaPluginExtension>().sourceSets
        project.extensions.getByType<NfExtension>().apply {
            version.set(neoForgeVersion)
            addModdingDependenciesTo(sourceSets["test"])
            mods {
                register(modId!!) {
                    modSourceSets.set(listOf(sourceSets["main"], sourceSets["test"]))
                }
            }
            runs {
                register("client") {
                    client()
                }
                register("server") {
                    server()
                    programArgument("--nogui")
                }
            }
            parchment {
                minecraftVersion.set(mcVersion)
                mappingsVersion.set(parchmentVersion)
            }
        }
        sourceSets["main"].resources.srcDirs.add(project.file("src/generated/resources"))
        project.tasks.withType<Jar>().configureEach {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            // These come in from the common API jars but should not end up in the neoforge jar
            exclude("fabric.mod.json")
            from("../LICENSE.md")
        }
    }

    fun gameTests() {
        project.dependencies.add("testImplementation", "net.neoforged:testframework:${neoForgeVersion}")
        val sourceSets = project.extensions.getByType<JavaPluginExtension>().sourceSets
        project.extensions.getByType<NfExtension>().apply {
            runs {
                register("gameTestServer") {
                    type.set("gameTestServer")
                    systemProperty("neoforge.enabledGameTestNamespaces", modId!!)
                    sourceSet.set(sourceSets["test"])
                }
            }
        }
        // This avoids a build failure when running the "test" task, because there is no JUnit engine
        // in this subproject.
        // The test source set in this subproject is used for Minecraft game tests, not for JUnit tests.
        project.tasks.getByName("test").onlyIf { false }
    }

    fun dataGeneration(sourceProject: Project = project) {
        project.extensions.getByType<NfExtension>().apply {
            runs {
                create("data") {
                    data()
                    programArgument("--mod")
                    programArgument(modId!!)
                    programArgument("--all")
                    programArgument("--output")
                    programArgument(sourceProject.file("src/generated/resources/").absolutePath)
                    programArgument("--existing")
                    programArgument(sourceProject.file("src/main/resources/").absolutePath)
                }
            }
        }
    }

    fun compileWithProject(dependency: Project) {
        project.evaluationDependsOn(":" + dependency.name)
        project.dependencies.add("compileOnly", dependency)
        project.dependencies.add("testCompileOnly", dependency)
        val sourceSets = dependency.extensions.getByType<JavaPluginExtension>().sourceSets
        project.tasks.withType<Jar>().configureEach {
            from(sourceSets["main"].output)
        }
        project.extensions.getByType<NfExtension>().apply {
            mods {
                getByName(modId!!) {
                    modSourceSets.add(sourceSets["main"])
                }
            }
        }
    }
}