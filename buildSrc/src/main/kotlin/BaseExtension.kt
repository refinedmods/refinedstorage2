import com.modrinth.minotaur.Minotaur
import com.modrinth.minotaur.ModrinthExtension
import info.solidsoft.gradle.pitest.PitestPlugin
import info.solidsoft.gradle.pitest.PitestPluginExtension
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

class PublishingOptions {
    var maven: Boolean? = false
    var curseForge: String? = null
    var modrinth: String? = null
}

open class BaseExtension(private val project: Project) {
    fun publishing(action: Action<PublishingOptions>) {
        val options = PublishingOptions()
        action.execute(options)
        if (options.maven == true) {
            enableMavenPublishing()
        }
        options.curseForge?.let { enableCurseForgePublishing(it) }
        options.modrinth?.let { enableModrinthPublishing(it) }
    }

    private fun enableCurseForgePublishing(projectId: String) {
        project.tasks.register<TaskPublishCurseForge>("publishCurseForge") {
            apiToken = System.getenv("CURSEFORGE_TOKEN")
            val isNeoForge = project.pluginManager.hasPlugin("net.neoforged.moddev")
            val mainFile = upload(projectId, project.tasks.getByName(if (isNeoForge) "jar" else "remapJar"))
            mainFile.releaseType = if (project.version.toString().contains("beta")) "beta" else if (project.version.toString().contains("alpha")) "alpha" else "release"
            mainFile.changelog = System.getenv("RELEASE_CHANGELOG")
            mainFile.changelogType = "markdown"
            mainFile.displayName = "v" + project.version.toString()
            mainFile.addGameVersion(mcVersion)
            if (isNeoForge) {
                mainFile.addModLoader("NeoForge")
            } else {
                mainFile.addRequirement("fabric-api")
            }
        }
    }

    private fun enableModrinthPublishing(projId: String) {
        project.plugins.apply(Minotaur::class.java)
        val isNeoForge = project.pluginManager.hasPlugin("net.neoforged.moddev")
        project.extensions.getByType<ModrinthExtension>().apply {
            token.set(System.getenv("MODRINTH_TOKEN"))
            projectId.set(projId)
            uploadFile = project.tasks.getByName(if (isNeoForge) "jar" else "remapJar")
            versionType.set(if (project.version.toString().contains("beta")) "beta" else if (project.version.toString().contains("alpha")) "alpha" else "release")
            versionNumber.set(project.version.toString())
            versionName.set("v" + project.version)
            if (isNeoForge) {
                loaders.add("neoforge")
                gameVersions.add(mcVersion)
            } else {
                dependencies.apply {
                    required.project("fabric-api")
                }
            }
            changelog.set(System.getenv("RELEASE_CHANGELOG"))
        }
    }

    fun mutationTesting() {
        project.plugins.apply(PitestPlugin::class.java)
        project.extensions.getByType<PitestPluginExtension>().apply {
            junit5PluginVersion.set("1.2.1")
            pitestVersion.set("1.15.3")
            outputFormats.set(listOf("HTML"))
            mutationThreshold.set(90)
            coverageThreshold.set(80)
        }
        project.dependencies.add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
                ?.because("required for pitest")
    }

    fun testing() {
        project.tasks.withType<Test>().forEach {
            it.useJUnitPlatform()
        }
    }

    fun javadoc() {
        project.extra["refinedarchitect_javadoc"] = true
    }

    private fun enableMavenPublishing() {
        project.extensions.getByType<PublishingExtension>().apply {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = project.uri("https://maven.pkg.github.com/" + System.getenv("GITHUB_REPOSITORY"))
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
                maven {
                    name = "CreeperHost"
                    url = project.uri("https://maven.creeperhost.net/release")
                    credentials {
                        username = System.getenv("CREEPERHOST_MAVEN_USERNAME")
                        password = System.getenv("CREEPERHOST_MAVEN_TOKEN")
                    }
                }
            }
            publications {
                create<MavenPublication>("mavenJava") {
                    from(project.components["java"])
                }
            }
        }
    }
}
