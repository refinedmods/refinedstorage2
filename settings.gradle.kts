dependencyResolutionManagement {
    repositories {
        maven {
            name = "Refined Architect"
            url = uri("https://maven.creeperhost.net")
            content {
                includeGroupAndSubgroups("com.refinedmods.refinedarchitect")
            }
        }
    }
    versionCatalogs {
        create("libs") {
            val refinedarchitectVersion: String by settings
            from("com.refinedmods.refinedarchitect:refinedarchitect-versioning:${refinedarchitectVersion}")
        }
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "Refined Architect"
            url = uri("https://maven.creeperhost.net")
            content {
                includeGroupAndSubgroups("com.refinedmods.refinedarchitect")
            }
        }
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
    val refinedarchitectVersion: String by settings
    plugins {
        id("com.refinedmods.refinedarchitect.root").version(refinedarchitectVersion)
        id("com.refinedmods.refinedarchitect.base").version(refinedarchitectVersion)
        id("com.refinedmods.refinedarchitect.common").version(refinedarchitectVersion)
        id("com.refinedmods.refinedarchitect.neoforge").version(refinedarchitectVersion)
        id("com.refinedmods.refinedarchitect.fabric").version(refinedarchitectVersion)
    }
}

rootProject.name = "refinedstorage"
include("refinedstorage-core-api")
include("refinedstorage-resource-api")
include("refinedstorage-storage-api")
include("refinedstorage-query-parser")
include("refinedstorage-autocrafting-api")
include("refinedstorage-network-api")
include("refinedstorage-network")
include("refinedstorage-common-api")
include("refinedstorage-common")
include("refinedstorage-fabric")
include("refinedstorage-fabric-api")
include("refinedstorage-neoforge")
include("refinedstorage-neoforge-api")
include("refinedstorage-network-test")
