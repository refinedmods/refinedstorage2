import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType

open class CommonExtension(private val project: Project) : BaseExtension(project) {
    fun common() {
        project.extensions.getByType<NeoForgeExtension>().apply {
            neoFormVersion.set(nfVersion)
            val sourceSets = project.extensions.getByType<JavaPluginExtension>().sourceSets
            addModdingDependenciesTo(sourceSets["test"])
            parchment {
                minecraftVersion.set(mcVersion)
                mappingsVersion.set(parchmentVersion)
            }
        }
        project.extensions.getByType<JavaPluginExtension>().apply {
            sourceSets["main"].resources.srcDir("src/generated/resources")
        }
        project.dependencies.add("compileOnly", "org.spongepowered:mixin:0.8.5")
    }
}