import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.extra

plugins {
    id("refinedarchitect")
}

tasks.register<Javadoc>("javadocAggregate") {
    val projects = allprojects.filter { it.extra.has("refinedarchitect_javadoc") }
    source(projects.flatMap { it.sourceSets["main"].allJava })
    classpath = files(projects.flatMap { it.sourceSets["main"].compileClasspath })
    setDestinationDir(file("build/docs/javadoc"))
}

tasks.register<JacocoReport>("codeCoverageReportAggregate") {
    subprojects.forEach { proj ->
        sourceSets(proj.extensions.getByType<JavaPluginExtension>().sourceSets["main"])
        proj.tasks.withType<Test>().forEach {
            dependsOn(it)
            executionData(it)
        }
    }
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

project.extensions.create("refinedarchitect", RootExtension::class, project)
