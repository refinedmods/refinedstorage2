plugins {
    id("refinedarchitect")
    id("net.neoforged.moddev")
}

repositories {
    maven {
        name = "ParchmentMC"
        url = project.uri("https://maven.parchmentmc.org")
    }
}

project.extensions.create("refinedarchitect", CommonExtension::class, project)
