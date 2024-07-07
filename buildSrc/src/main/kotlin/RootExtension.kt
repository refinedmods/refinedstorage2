import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.sonarqube.gradle.SonarExtension
import org.sonarqube.gradle.SonarQubePlugin

open class RootExtension(private val project: Project) {
    fun sonarQube(projectKey: String, organization: String = "refinedmods") {
        System.setProperty("sonar.gradle.skipCompile", "true")
        project.plugins.apply(SonarQubePlugin::class.java)
        project.extensions.getByType<SonarExtension>().apply {
            // https://docs.sonarqube.org/latest/analysis/github-integration/
            properties {
                property("sonar.projectKey", projectKey)
                property("sonar.organization", organization)
                property("sonar.host.url", "https://sonarcloud.io")
                property("sonar.coverage.jacoco.xmlReportPaths", "${project.layout.buildDirectory.get()}/reports/jacoco/codeCoverageReport/codeCoverageReport.xml")
            }
        }
    }
}