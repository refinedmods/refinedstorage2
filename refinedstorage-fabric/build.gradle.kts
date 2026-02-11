plugins {
    id("com.refinedmods.refinedarchitect.fabric")
}

repositories {
    maven {
        name = "ModMenu"
        url = uri("https://maven.terraformersmc.com/")
    }
    maven {
        name = "Cloth Config"
        url = uri("https://maven.shedaniel.me/")
    }
}

refinedarchitect {
    modId = "refinedstorage"
    fabric()
    publishing {
        maven = true
        curseForge = "243076"
        curseForgeRequiredDependencies = listOf("fabric-api")
        modrinth = "KDvYkUg3"
        modrinthRequiredDependencies = listOf("fabric-api")
    }
}

base {
    archivesName.set("refinedstorage-fabric")
}

val commonJava by configurations.existing
val commonResources by configurations.existing

dependencies {
    modApi(libs.cloth.config) {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modApi(libs.teamreborn.energy) {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modApi(libs.modmenu)
    include(libs.cloth.config)
    include(libs.teamreborn.energy)

    testCompileOnly(libs.apiguardian)

    compileOnly(project(":refinedstorage-common"))
    compileOnly(project(":refinedstorage-common-api"))
    compileOnly(project(":refinedstorage-fabric-api"))
    compileOnly(project(":refinedstorage-core-api"))
    compileOnly(project(":refinedstorage-resource-api"))
    compileOnly(project(":refinedstorage-storage-api"))
    compileOnly(project(":refinedstorage-network-api"))
    compileOnly(project(":refinedstorage-network"))
    compileOnly(project(":refinedstorage-autocrafting-api"))
    compileOnly(project(":refinedstorage-query-parser"))
    commonJava(project(path = ":refinedstorage-common", configuration = "commonJava"))
    commonResources(project(path = ":refinedstorage-common", configuration = "commonResources"))
    commonJava(project(path = ":refinedstorage-common-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-fabric-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-core-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-resource-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-storage-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-network-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-network", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-autocrafting-api", configuration = "commonJava"))
    commonJava(project(path = ":refinedstorage-query-parser", configuration = "commonJava"))
}
